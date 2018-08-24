package ru.paymon.android.utils;

import android.util.Log;
import android.util.LongSparseArray;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ru.paymon.android.Config;
import ru.paymon.android.MediaManager;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.Packet;
import ru.paymon.android.net.RPC;


public class FileManager {
    public interface IDownloadingFile {
        void onFinish();

        void onProgress(int percent);

        void onError(int code);
    }

    public interface IUploadingFile {
        void onFinish();

        void onProgress(int percent);

        void onError(int code);
    }

    public enum FileType {
        NONE,
        PHOTO,
        AUDIO,
        DOCUMENT,
        STICKER,
        ACTION,
        WALLET,
        VIDEO //TODO:Сделать обработку для видео
    }

    public static class DownloadingFile {
        public SerializedBuffer buffer;
        public IDownloadingFile listener;
        public int partsCount;
        public int currentPart;
        public int currentDownloaded;
        public long id;
        public String name;
    }

    public static class UploadingFile {
        public FileType type;
        public byte state;
        public SerializedBuffer buffer;
        public IUploadingFile listener;
        public int partsCount;
        public int currentPart;
        public int currentUploaded;
        public long id;
        public String name;
        public int fileSize;
        public int uploadChunkSize;

    }

    private LongSparseArray<DownloadingFile> downloadingFiles = new LongSparseArray<>();
    private LongSparseArray<UploadingFile> uploadingFiles = new LongSparseArray<>();

    private static volatile FileManager Instance = null;

    public static FileManager getInstance() {
        FileManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new FileManager();
                }
            }
        }
        return localInstance;
    }

    private FileManager() {
    }

    public void startUploading(RPC.PM_photo photo, IUploadingFile listener) {
        if (uploadingFiles.get(photo.id) != null) return;
        File photoFile = MediaManager.getInstance().getFile(photo.user_id, photo.id);
        if (photoFile == null) return;

        byte bytes[] = new byte[(int) photoFile.length()];
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(photoFile)));
            dis.readFully(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final UploadingFile uploadingFile = new UploadingFile();

        uploadingFile.fileSize = (int) photoFile.length();
        uploadingFile.uploadChunkSize = Math.max(32, (uploadingFile.fileSize + 1024 * 3000 - 1) / (1024 * 3000));
        if (1024 % uploadingFile.uploadChunkSize != 0) {
            int chunkSize = 64;
            while (uploadingFile.uploadChunkSize > chunkSize) {
                chunkSize *= 2;
            }
            uploadingFile.uploadChunkSize = chunkSize;
        }
        uploadingFile.uploadChunkSize *= 1024;
        uploadingFile.partsCount = (uploadingFile.fileSize + uploadingFile.uploadChunkSize - 1) / uploadingFile.uploadChunkSize;
        uploadingFile.type = FileType.PHOTO;
        uploadingFile.buffer = BuffersStorage.getInstance().getFreeBuffer(uploadingFile.fileSize);
        uploadingFile.buffer.limit(uploadingFile.fileSize);
        uploadingFile.buffer.writeBytes(bytes);
        uploadingFile.buffer.position(0);
        uploadingFile.listener = listener;
        uploadingFile.currentPart = 0;
        uploadingFile.currentUploaded = 0;
        uploadingFile.name = "photo.jpg";
        uploadingFile.id = photo.id;

        final RPC.PM_file file = new RPC.PM_file();
        file.id = uploadingFile.id;
        file.partsCount = uploadingFile.partsCount;
        file.totalSize = uploadingFile.fileSize;
        file.type = FileType.PHOTO;
        Log.d(Config.TAG, "Uploading file. parts=" + file.partsCount + ", size=" + file.totalSize + ", id=" + file.id);

        uploadingFiles.put(file.id, uploadingFile);

        NetworkManager.getInstance().sendRequest(file, (response, error) -> {
            if (response != null && error == null) {
                if (response instanceof RPC.PM_boolTrue) {
                    continueFileUpload(uploadingFile.id);
                    if (uploadingFile.listener != null) {
                        uploadingFile.listener.onProgress(1);
                    }
                } else {
                    if (uploadingFile.listener != null) {
                        uploadingFile.listener.onError(3);
                    }
                    cancelFileUpload(uploadingFile.id);
                }
            }
        });

    }

    public void continueFileUpload(long fileID) {
        final UploadingFile uploadingFile = uploadingFiles.get(fileID);
        if (uploadingFile == null) return;

        int bytesToSendCount = Math.min(uploadingFile.uploadChunkSize, uploadingFile.fileSize - uploadingFile.currentUploaded);
        if (bytesToSendCount <= 0) {
            cancelFileUpload(uploadingFile.id);
            return;
        }
        byte bytes[] = new byte[bytesToSendCount];
//        memcpy(ba->bytes, imgBytes + currentUploaded, (size_t) bytesToSendCount);
        try {
            uploadingFile.buffer.readBytes(bytes, true);
        } catch (Exception e) {
            Log.e(Config.TAG, "Can't read uploading file buffer");
            if (uploadingFile.listener != null) {
                uploadingFile.listener.onError(2);
            }
            cancelFileUpload(uploadingFile.id);
            return;
        }
        RPC.PM_filePart filePart = new RPC.PM_filePart();
        filePart.fileID = uploadingFile.id;
        filePart.part = uploadingFile.currentPart;
        filePart.bytes = bytes;

        uploadingFile.currentUploaded += bytesToSendCount;

        NetworkManager.getInstance().sendRequest(filePart, (response, error) -> {
            if (response != null && error == null) {
                if (response instanceof RPC.PM_boolTrue) {
                    uploadingFile.currentPart++;
                    if (uploadingFile.currentPart == uploadingFile.partsCount || uploadingFile.currentUploaded >= uploadingFile.fileSize) {
                        uploadingFile.state = 2;
                        if (uploadingFile.listener != null) {
                            uploadingFile.listener.onFinish();
                        }
                        uploadingFiles.remove(uploadingFile.id);
                        return;
                    }
                    continueFileUpload(uploadingFile.id);
                } else {
                    if (uploadingFile.listener != null) {
                        uploadingFile.listener.onError(1);
                    }
                    cancelFileUpload(uploadingFile.id);
                }
            }
        });
    }

    public void cancelFileUpload(long fileID) {
        if (uploadingFiles.get(fileID) == null) return;
        Log.d(Config.TAG, "File upload canceled");
        uploadingFiles.remove(fileID);
    }

    public void acceptFileDownload(RPC.PM_file file, long messageID) {
        if (downloadingFiles.get(file.id) != null) return;

        final DownloadingFile downloadingFile = new DownloadingFile();
        Log.d(Config.TAG, "Downloading file. parts=" + file.partsCount + ", size=" + file.totalSize + ", id=" + file.id);
        downloadingFile.buffer = BuffersStorage.getInstance().getFreeBuffer(file.totalSize);
        downloadingFile.listener = new IDownloadingFile() {
            @Override
            public void onFinish() {
                Log.d(Config.TAG, "File has downloaded");
                MediaManager.getInstance().saveAndUpdatePhoto(downloadingFile);
            }

            @Override
            public void onProgress(int percent) {

            }

            @Override
            public void onError(int code) {
                Log.e(Config.TAG, "file download failed " + code);
            }
        };
        downloadingFile.currentPart = 0;
        downloadingFile.currentDownloaded = 0;
        downloadingFile.partsCount = file.partsCount;
        downloadingFile.name = file.name;
        downloadingFile.id = file.id;
        downloadingFiles.put(file.id, downloadingFile);

        NetworkManager.getInstance().sendRequest(new RPC.PM_boolTrue(), null, messageID);
    }

    public void acceptStickerDownload(RPC.PM_file file, long messageID) {
        if (downloadingFiles.get(file.id) != null) return;

        final DownloadingFile downloadingFile = new DownloadingFile();
        Log.d(Config.TAG, "Downloading sticker. parts=" + file.partsCount + ", size=" + file.totalSize + ", id=" + file.id);
        downloadingFile.buffer = BuffersStorage.getInstance().getFreeBuffer(file.totalSize);
        downloadingFile.listener = new IDownloadingFile() {
            @Override
            public void onFinish() {
                Log.d(Config.TAG, "Sticker has downloaded");
                MediaManager.getInstance().saveAndUpdateSticker(downloadingFile);
            }

            @Override
            public void onProgress(int percent) {

            }

            @Override
            public void onError(int code) {
                Log.e(Config.TAG, "Sticker download failed " + code);
            }
        };
        downloadingFile.currentPart = 0;
        downloadingFile.currentDownloaded = 0;
        downloadingFile.partsCount = file.partsCount;
        downloadingFile.name = file.name;
        downloadingFile.id = file.id;
        downloadingFiles.put(file.id, downloadingFile);

        RPC.PM_boolTrue packet = new RPC.PM_boolTrue();
        NetworkManager.getInstance().sendRequest(packet, null, messageID);
    }

    public void continueFileDownload(RPC.PM_filePart part, long messageID) {
        DownloadingFile downloadingFile = downloadingFiles.get(part.fileID);
        if (downloadingFile != null) {
            if (part.part == downloadingFile.currentPart) {
                downloadingFile.currentDownloaded += part.bytes.length;
                Log.d(Config.TAG, "Downloading... " + downloadingFile.currentDownloaded + "/" + downloadingFile.buffer.limit());
                downloadingFile.buffer.writeBytes(part.bytes);
                downloadingFile.currentPart++;
                if (downloadingFile.currentPart == downloadingFile.partsCount) {
                    if (downloadingFile.listener != null) {
                        downloadingFile.listener.onFinish();
                    }
                    downloadingFiles.remove(part.fileID);
                }
                RPC.PM_boolTrue packet = new RPC.PM_boolTrue();
                NetworkManager.getInstance().sendRequest(packet, null, messageID);
            } else {
                Log.e(Config.TAG, "Error 1");
                if (downloadingFile.listener != null) {
                    downloadingFile.listener.onError(1);
                    RPC.PM_boolFalse packet = new RPC.PM_boolFalse();
                    NetworkManager.getInstance().sendRequest(packet, null, messageID);
                }
            }
        } else {
            Log.e(Config.TAG, "Error 2");
            RPC.PM_boolFalse packet = new RPC.PM_boolFalse();
            NetworkManager.getInstance().sendRequest(packet, null, messageID);
        }
    }
}
