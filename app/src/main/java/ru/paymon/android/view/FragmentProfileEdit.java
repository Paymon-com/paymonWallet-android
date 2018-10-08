package ru.paymon.android.view;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.rxpermission.Permission;

import androidx.navigation.Navigation;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.Utils;

import static android.app.Activity.RESULT_OK;

//import ru.paymon.android.utils.ImagePicker;


public class FragmentProfileEdit extends Fragment {
    private DialogProgress dialogProgress;
    private CircularImageView avatar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        avatar = (CircularImageView) view.findViewById(R.id.profile_update_photo);
        TextView changeAvatar = (TextView) view.findViewById(R.id.change_foto);
        TextInputEditText firstName = (TextInputEditText) view.findViewById(R.id.profile_update_name);
        TextInputEditText lastName = (TextInputEditText) view.findViewById(R.id.profile_update_surname);
        EditText email = (EditText) view.findViewById(R.id.profile_update_email);
        Button saveButton = (Button) view.findViewById(R.id.profile_update_save_button);
        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);
        Switch hideEmailSwitch = (Switch) view.findViewById(R.id.switch_hide_email);

        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        if (!User.currentUser.photoURL.url.isEmpty())
            Utils.loadPhoto(User.currentUser.photoURL.url, avatar);

        firstName.setText(User.currentUser.first_name);
        lastName.setText(User.currentUser.last_name);
        email.setText(User.currentUser.email != null ? User.currentUser.email : "");

        View.OnClickListener avatarListener = (v) -> {
            ApplicationLoader.rxPermission
                    .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Permission>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Permission permission) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    if (ApplicationLoader.rxPermission.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && ApplicationLoader.rxPermission.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                        ImagePicker.create(FragmentProfileEdit.this)
//                                .returnMode(ReturnMode.ALL)
//                                .toolbarFolderTitle("Folder")
//                                .toolbarImageTitle("Tap to select")
//                                .toolbarArrowColor(Color.BLACK)
//                                .includeVideo(true)
//                                .single()
//                                .showCamera(true)
//                                .imageDirectory("Camera")
////                                  .theme(R.style.CustomImagePickerTheme) // must inherit ef_BaseTheme. please refer to sample
//                                .enableLog(false) // disabling log
//                                .imageLoader(new PicassoImageLoader())
//                                .start();
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.OFF)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .setFixAspectRatio(true)
                                .setMinCropWindowSize(Config.minAvatarSize, Config.minAvatarSize)
                                .start(getContext(), FragmentProfileEdit.this);
                    } else {
                        Toast.makeText(getContext(), "Недостаточно прав!", Toast.LENGTH_LONG).show(); //TODO:String
                    }
                }
            });
        };

        avatar.setOnClickListener(avatarListener);
        changeAvatar.setOnClickListener(avatarListener);

        saveButton.setOnClickListener((v) -> {
            if (!Utils.nameCorrect(firstName.getText().toString())) {
                Toast.makeText(getActivity(), getString(R.string.reg_name_correct), Toast.LENGTH_SHORT).show();
                firstName.requestFocus();
                return;
            }

            if (!Utils.nameCorrect(lastName.getText().toString())) {
                Toast.makeText(getActivity(), getString(R.string.reg_surname_correct), Toast.LENGTH_SHORT).show();
                lastName.requestFocus();
                return;
            }

            if (!Utils.emailCorrect(email.getText().toString())) {
                Toast.makeText(getActivity(), getString(R.string.reg_check_email), Toast.LENGTH_SHORT).show();
                email.requestFocus();
                return;
            }

            Utils.netQueue.postRunnable(() -> {
                RPC.PM_userSelf user = User.currentUser;
                user.first_name = firstName.getText().toString();
                user.last_name = lastName.getText().toString();
                user.email = email.getText().toString();

                ApplicationLoader.applicationHandler.post(dialogProgress::show);

                final long requestID = NetworkManager.getInstance().sendRequest(user, (response, error) -> {
                    if (error != null || response instanceof RPC.PM_boolFalse) {
                        ApplicationLoader.applicationHandler.post(() -> {
                            if (dialogProgress != null && dialogProgress.isShowing())
                                dialogProgress.cancel();

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                    .setMessage(getString(R.string.profile_edit_failed))
                                    .setCancelable(false);
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        });
                        return;
                    }

                    if (response instanceof RPC.PM_boolTrue) {
                        User.currentUser = user;
                        User.saveConfig();

                        ApplicationLoader.applicationHandler.post(() -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                    .setMessage(getString(R.string.profile_edit_success))
                                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                    })
                                    .setCancelable(true);
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        });
                    }

                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.dismiss();
                    });
                });

                ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
            });
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Utils.netQueue.postRunnable(() -> {
                    ApplicationLoader.applicationHandler.post(dialogProgress::show);

                    final RPC.PM_setProfilePhoto setProfilePhotoRequest = new RPC.PM_setProfilePhoto();

                    final long requestID = NetworkManager.getInstance().sendRequest(setProfilePhotoRequest, (response, error) -> {
                        if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                            ApplicationLoader.applicationHandler.post(() -> {
                                if (dialogProgress != null && dialogProgress.isShowing())
                                    dialogProgress.cancel();

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                        .setMessage(R.string.photo_upload_failed) //TODO:string
                                        .setCancelable(true);
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            });
                            return;
                        }

                        if (response instanceof RPC.PM_boolTrue) {
                            FileManager.getInstance().startUploading(result.getUri().getPath(), true, Config.maxAvatarSize, 85, new FileManager.IUploadingFile() {
                                @Override
                                public void onFinish() {
                                    Log.e(Config.TAG, "Profile photoURL successfully uploaded");
                                    ApplicationLoader.applicationHandler.post(() -> {
                                        if (dialogProgress != null && dialogProgress.isShowing())
                                            dialogProgress.dismiss();
                                        if (!User.currentUser.photoURL.url.isEmpty())
                                            Utils.loadPhoto(User.currentUser.photoURL.url, avatar);
                                    });
                                }

                                @Override
                                public void onProgress(int percent) {

                                }

                                @Override
                                public void onError(int code) {
                                    Log.e(Config.TAG, "Error while uploading profile photoURL, error code: " + code);
                                    ApplicationLoader.applicationHandler.post(() -> {
                                        if (dialogProgress != null && dialogProgress.isShowing())
                                            dialogProgress.cancel();
                                    });
                                }
                            });
                        }

                        ApplicationLoader.applicationHandler.post(() -> {
                            if (dialogProgress != null && dialogProgress.isShowing())
                                dialogProgress.dismiss();
                        });
                    });

                    ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("AAA", error.getMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
