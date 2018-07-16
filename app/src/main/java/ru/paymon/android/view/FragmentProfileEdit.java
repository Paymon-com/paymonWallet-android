package ru.paymon.android.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.MainActivity;
import ru.paymon.android.MediaManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.ObservableMediaManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.ImagePicker;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.Config.CAMERA_PERMISSIONS;


public class FragmentProfileEdit extends Fragment {
    private static final int PICK_IMAGE_ID = 100;
    private static FragmentProfileEdit instance;
    private DialogProgress dialogProgress;
    private CircleImageView avatar;

    public static synchronized FragmentProfileEdit newInstance() {
        instance = new FragmentProfileEdit();
        return instance;
    }

    public static synchronized FragmentProfileEdit getInstance() {
        if (instance == null)
            instance = new FragmentProfileEdit();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        avatar = (CircleImageView) view.findViewById(R.id.profile_update_photo);
        TextView changeAvatar = (TextView) view.findViewById(R.id.change_foto);
        TextInputEditText firstName = (TextInputEditText) view.findViewById(R.id.profile_update_name);
        TextInputEditText lastName = (TextInputEditText) view.findViewById(R.id.profile_update_surname);
        TextInputEditText birthday = (TextInputEditText) view.findViewById(R.id.profile_update_bday);
        TextInputEditText phone = (TextInputEditText) view.findViewById(R.id.profile_update_phone_edit_text);
        TextInputEditText city = (TextInputEditText) view.findViewById(R.id.profile_update_city);
        TextInputEditText country = (TextInputEditText) view.findViewById(R.id.profile_update_country);
        TextInputEditText email = (TextInputEditText) view.findViewById(R.id.profile_update_email);
        CheckBox male = (CheckBox) view.findViewById(R.id.profile_update_male);
        CheckBox female = (CheckBox) view.findViewById(R.id.profile_update_female);
        Button saveButton = (Button) view.findViewById(R.id.profile_update_save_button);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        RPC.PM_photo photo = new RPC.PM_photo();
        photo.user_id = User.currentUser.id;
        photo.id = User.currentUser.photoID;
        avatar.setPhoto(photo);

        firstName.setText(User.currentUser.first_name);
        lastName.setText(User.currentUser.last_name);
        email.setText(User.currentUser.email);
        phone.setText(Utils.formatPhone(User.currentUser.phoneNumber));
        city.setText(User.currentUser.city);
        birthday.setText(User.currentUser.birthdate);
        country.setText(User.currentUser.country);

        switch (User.currentUser.gender) {
            case 0:
                female.setChecked(false);
                male.setChecked(false);
                break;
            case 1:
                female.setChecked(false);
                male.setChecked(true);
                break;
            case 2:
                female.setChecked(true);
                male.setChecked(false);
                break;
        }

        birthday.setOnClickListener((view1) -> setDate(birthday));

        View.OnClickListener avatarListener = (view1) -> {
            ((MainActivity) getActivity()).requestAppPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    R.string.msg_permissions_required, CAMERA_PERMISSIONS);
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(ApplicationLoader.applicationContext, "выберите");//TODO:string
            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
        };

        avatar.setOnClickListener(avatarListener);
        changeAvatar.setOnClickListener(avatarListener);

        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher(){
            @Override
            public synchronized void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("+")){
                    phone.setText(s.insert(0, "+"));
                    Selection.setSelection(phone.getText(), phone.getText().length());
                }
                super.afterTextChanged(s);
            }
        });

        saveButton.setOnClickListener((view1) -> {
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

            if (!Utils.phoneCorrect(phone.getText().toString())) {
                Toast.makeText(getActivity(), getString(R.string.reg_phone_correct), Toast.LENGTH_SHORT).show();
                phone.requestFocus();
                return;
            }

            if (!Utils.emailCorrect(email.getText().toString())) {
                Toast.makeText(getActivity(), getString(R.string.reg_check_email), Toast.LENGTH_SHORT).show();
                email.requestFocus();
                return;
            }

            Utils.netQueue.postRunnable(() -> {
                RPC.PM_userFull user = User.currentUser;
                user.gender = (!male.isChecked() && !female.isChecked()) ? 0 : male.isChecked() ? 1 : 2;
                user.first_name = firstName.getText().toString();
                user.last_name = lastName.getText().toString();
                user.birthdate = birthday.getText().toString();
                user.phoneNumber = phone.getText().toString().isEmpty() ? 0 : Long.parseLong(phone.getText().toString().replace(" ", "").replace("+", "").replace("-", "").replace("(","").replace(")",""));
                user.city = city.getText().toString();
                user.country = country.getText().toString();
                user.email = email.getText().toString();

                ApplicationLoader.applicationHandler.post(dialogProgress::show);

                final long requestID = NetworkManager.getInstance().sendRequest(user, (response, error) -> {
                    if (error != null || (response != null && response instanceof RPC.PM_boolFalse)) {
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
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_update_profile));
        Utils.setArrowBackInToolbar(getActivity());
    }

    private void setDate(TextInputEditText birthday) {
        Calendar date = Calendar.getInstance();
        new DatePickerDialog(getActivity(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    date.set(Calendar.YEAR, year);
                    date.set(Calendar.MONTH, monthOfYear);
                    date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    birthday.setText((String) DateFormat.format("yyyy-MM-dd", date));
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_ID:
                if (resultCode == Activity.RESULT_OK) {
                    Utils.netQueue.postRunnable(() -> {
                        Bitmap bitmap = ImagePicker.getImageFromResult(ApplicationLoader.applicationContext, requestCode, resultCode, data);

                        ApplicationLoader.applicationHandler.post(dialogProgress::show);

                        final RPC.PM_photo newPhoto = MediaManager.getInstance().savePhoto(bitmap, User.currentUser);
                        final RPC.PM_setProfilePhoto setProfilePhotoRequest = new RPC.PM_setProfilePhoto();
                        setProfilePhotoRequest.photo = newPhoto;

                        ApplicationLoader.applicationHandler.post(() -> avatar.setPhoto(newPhoto));


                        final long oldPhotoID = User.currentUser.photoID;
                        final long newPhotoID = newPhoto.id;

                        final long requestID = NetworkManager.getInstance().sendRequest(setProfilePhotoRequest, (response, error) -> {
                            if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                                ApplicationLoader.applicationHandler.post(() -> {
                                    if (dialogProgress != null && dialogProgress.isShowing())
                                        dialogProgress.cancel();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                            .setMessage("Фотографию загрузить не удалось") //TODO:string
                                            .setCancelable(true);
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                });
                                return;
                            }

                            if (response instanceof RPC.PM_boolTrue) {
                                FileManager.getInstance().startUploading(newPhoto, new FileManager.IUploadingFile() {
                                    @Override
                                    public void onFinish() {
                                        Log.d(Config.TAG, "Profile photo successfully uploaded");
                                        ApplicationLoader.applicationHandler.post(() -> {
                                            if (dialogProgress != null && dialogProgress.isShowing())
                                                dialogProgress.dismiss();

//                                            avatar.setPhoto(newPhoto);
//                                            User.currentUser.photoID = newPhoto.id;
//                                            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.PROFILE_PHOTO_UPDATED, User.currentUser.id, newPhoto, bitmap);
//                                            ObservableMediaManager.getInstance().postPhotoUpdateIDNotification(oldPhotoID, newPhotoID);
//                                            NotificationManager.getInstance().postNotificationName(NotificationManager.didPhotoUpdate, bitmap);
//                                            avatar.setImageBitmap(bitmap);
                                        });
                                    }

                                    @Override
                                    public void onProgress(int percent) {

                                    }

                                    @Override
                                    public void onError(int code) {
                                        Log.d(Config.TAG, "Error while uploading profile photo, error code: " + code);
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

                        ApplicationLoader.applicationHandler.post(() -> {
                            dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false));
                        });
                    });
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
