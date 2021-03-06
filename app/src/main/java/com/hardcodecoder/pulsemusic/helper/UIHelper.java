package com.hardcodecoder.pulsemusic.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.hardcodecoder.pulsemusic.PulseController;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.dialog.AddToPlaylistDialog;
import com.hardcodecoder.pulsemusic.dialog.base.RoundedCustomBottomSheet;
import com.hardcodecoder.pulsemusic.interfaces.CreatePlaylist;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.providers.ProviderManager;
import com.hardcodecoder.pulsemusic.utils.DataUtils;
import com.hardcodecoder.pulsemusic.utils.NavigationUtil;
import com.hardcodecoder.pulsemusic.views.MediaArtImageView;

public class UIHelper {

    public static void showMenuForLibraryTracks(@NonNull Activity activity, @NonNull FragmentManager fragmentManager, @NonNull MusicModel data) {
        buildAndShowOptionsMenu(activity, fragmentManager, data, true);
    }

    public static void showMenuForAlbumDetails(@NonNull Activity activity, @NonNull FragmentManager fragmentManager, @NonNull MusicModel data) {
        buildAndShowOptionsMenu(activity, fragmentManager, data, false);
    }

    public static void buildCreatePlaylistDialog(@NonNull Context context, @NonNull CreatePlaylist callback) {
        BottomSheetDialog sheetDialog = new RoundedCustomBottomSheet(context, RoundedCustomBottomSheet::setDefaultBehaviour);
        View layout = View.inflate(context, R.layout.bottom_dialog_edit_text, null);
        sheetDialog.setContentView(layout);

        TextView header = layout.findViewById(R.id.header);
        header.setText(context.getString(R.string.create_playlist));

        TextInputLayout til = layout.findViewById(R.id.edit_text_container);
        til.setHint(context.getString(R.string.create_playlist_hint));

        TextInputEditText et = layout.findViewById(R.id.text_input_field);

        layout.findViewById(R.id.confirm_btn).setOnClickListener(v -> {
            if (et.getText() != null) {
                String text = et.getText().toString().trim();
                if (text.length() == 0 || text.charAt(0) == ' ') {
                    Toast.makeText(context, context.getString(R.string.create_playlist_hint), Toast.LENGTH_SHORT).show();
                    return;
                }
                String playlistName = et.getText().toString();
                ProviderManager.getPlaylistProvider().addPlaylistItem(playlistName);
                callback.onPlaylistCreated(playlistName);
            } else {
                Toast.makeText(context, context.getString(R.string.create_playlist_hint), Toast.LENGTH_SHORT).show();
                return;
            }
            dismiss(sheetDialog);
        });

        layout.findViewById(R.id.cancel_btn).setOnClickListener(v -> dismiss(sheetDialog));
        sheetDialog.show();
    }

    public static void buildSongInfoDialog(@NonNull Context context, @NonNull final MusicModel musicModel) {
        BottomSheetDialog bottomSheetDialog = new RoundedCustomBottomSheet(context, RoundedCustomBottomSheet::setDefaultBehaviour);
        final View view = View.inflate(context, R.layout.bottom_sheet_track_info, null);
        bottomSheetDialog.setContentView(view);
        view.findViewById(R.id.dialog_ok).setOnClickListener(v -> dismiss(bottomSheetDialog));
        // Reference view fields which needs to be filled with data
        MaterialTextView displayTextView = view.findViewById(R.id.dialog_display_name);
        MaterialTextView trackTitle = view.findViewById(R.id.dialog_track_title);
        MaterialTextView trackAlbum = view.findViewById(R.id.dialog_track_album);
        MaterialTextView trackArtist = view.findViewById(R.id.dialog_track_artist);
        MaterialTextView trackFileSize = view.findViewById(R.id.dialog_file_size);
        MaterialTextView trackFileType = view.findViewById(R.id.dialog_file_type);
        MaterialTextView trackBitRate = view.findViewById(R.id.dialog_bitrate);
        MaterialTextView trackSampleRate = view.findViewById(R.id.dialog_sample_rate);
        MaterialTextView trackChannelCount = view.findViewById(R.id.dialog_channel_count);
        bottomSheetDialog.show();
        DataModelHelper.getTrackInfo(view.getContext(), musicModel, infoModel -> {
            if (null != infoModel) {
                view.postOnAnimation(() -> {
                    final StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
                    displayTextView.setText(infoModel.getDisplayName());
                    displayTextView.setSelected(true);
                    setInfo(trackTitle, context.getString(R.string.head_title), musicModel.getTrackName(), styleSpan);
                    setInfo(trackAlbum, context.getString(R.string.albums), musicModel.getAlbum(), styleSpan);
                    setInfo(trackArtist, context.getString(R.string.artist), musicModel.getArtist(), styleSpan);
                    setInfo(trackFileSize, context.getString(R.string.head_file_size), DataUtils.getFormattedFileSize(infoModel.getFileSize()), styleSpan);
                    setInfo(trackFileType, context.getString(R.string.head_file_type), infoModel.getFileType(), styleSpan);
                    setInfo(trackBitRate, context.getString(R.string.head_bitrate), DataUtils.getFormattedBitRate(infoModel.getBitRate()), styleSpan);
                    setInfo(trackSampleRate, context.getString(R.string.head_sample_rate), DataUtils.getFormattedSampleRate(infoModel.getSampleRate()), styleSpan);
                    setInfo(trackChannelCount, context.getString(R.string.head_channel_count), String.valueOf(infoModel.getChannelCount()), styleSpan);
                    bottomSheetDialog.show();
                });
            }
        });
    }

    private static void setInfo(@NonNull MaterialTextView textView, @NonNull String head, @NonNull String info, @NonNull StyleSpan styleSpan) {
        String text = String.format("%s: %s", head, info);
        SpannableString sub = new SpannableString(text);
        sub.setSpan(styleSpan, 0, head.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(sub);
    }

    public static void openAddToPlaylistDialog(@NonNull FragmentManager fragmentManager, @NonNull final MusicModel itemToAdd) {
        AddToPlaylistDialog dialog = AddToPlaylistDialog.getInstance();
        Bundle b = new Bundle();
        b.putSerializable(AddToPlaylistDialog.MUSIC_MODEL_KEY, itemToAdd);
        dialog.setArguments(b);
        dialog.show(fragmentManager, AddToPlaylistDialog.TAG);
    }

    private static void buildAndShowOptionsMenu(@NonNull Activity activity,
                                                @NonNull FragmentManager fragmentManager,
                                                @NonNull final MusicModel data,
                                                boolean showGoToAlbums) {
        View view = View.inflate(activity, R.layout.library_item_menu, null);
        BottomSheetDialog bottomSheetDialog = new RoundedCustomBottomSheet(activity, RoundedCustomBottomSheet::setDefaultBehaviour);
        MediaArtImageView trackAlbumArt = view.findViewById(R.id.track_album_art);
        trackAlbumArt.setTransitionName("song_info_transition_" + data.getId());
        MaterialTextView trackTitle = view.findViewById(R.id.track_title);
        MaterialTextView trackSubTitle = view.findViewById(R.id.track_sub_title);

        trackAlbumArt.loadAlbumArt(data.getAlbumArtUrl(), data.getAlbumId());
        trackTitle.setText(data.getTrackName());
        trackTitle.setSelected(true);
        trackSubTitle.setText(data.getArtist());

        view.findViewById(R.id.track_play_next).setOnClickListener(v -> {
            PulseController.getInstance().playNext(data);
            Toast.makeText(v.getContext(), activity.getString(R.string.play_next_toast), Toast.LENGTH_SHORT).show();
            dismiss(bottomSheetDialog);
        });

        view.findViewById(R.id.add_to_queue).setOnClickListener(v -> {
            PulseController.getInstance().addToQueue(data);
            Toast.makeText(v.getContext(), activity.getString(R.string.add_to_queue_toast), Toast.LENGTH_SHORT).show();
            dismiss(bottomSheetDialog);
        });

        view.findViewById(R.id.song_info).setOnClickListener(v -> {
            buildSongInfoDialog(activity, data);
            dismiss(bottomSheetDialog);
        });

        if (showGoToAlbums) {
            MaterialTextView goToAlbumsOption = view.findViewById(R.id.go_to_album);
            goToAlbumsOption.setVisibility(View.VISIBLE);
            goToAlbumsOption.setOnClickListener(v ->
                    NavigationUtil.goToAlbum(activity, trackAlbumArt, data.getAlbum(), data.getAlbumId(), data.getAlbumArtUrl()));
        }

        view.findViewById(R.id.go_to_artist).setOnClickListener(v ->
                NavigationUtil.goToArtist(activity, data.getArtist()));

        view.findViewById(R.id.add_to_playlist).setOnClickListener(v -> {
            openAddToPlaylistDialog(fragmentManager, data);
            dismiss(bottomSheetDialog);
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private static void dismiss(@NonNull BottomSheetDialog dialog) {
        if (dialog.isShowing())
            dialog.dismiss();
    }
}