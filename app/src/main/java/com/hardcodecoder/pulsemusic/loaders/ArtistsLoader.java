package com.hardcodecoder.pulsemusic.loaders;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import com.hardcodecoder.pulsemusic.model.ArtistModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ArtistsLoader implements Callable<List<ArtistModel>> {

    private final ContentResolver mContentResolver;
    private final String mSortOrder;

    ArtistsLoader(ContentResolver mContentResolver, SortOrder.ARTIST sortOrder) {
        this.mContentResolver = mContentResolver;
        mSortOrder = MediaStoreHelper.getSortOrderFor(sortOrder);
    }

    @Override
    public List<ArtistModel> call() {
        List<ArtistModel> artistList = new ArrayList<>();
        String[] col = {MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS};

        final Cursor cursor = mContentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                col,
                null,
                null,
                mSortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int artistIdColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
            int artistColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
            int albumCountColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            int trackCountColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);

            do {
                int artistId = cursor.getInt(artistIdColumnIndex);
                String artist = cursor.getString(artistColumnIndex);
                int num_albums = cursor.getInt(albumCountColumnIndex);
                int num_tracks = cursor.getInt(trackCountColumnIndex);
                artistList.add(new ArtistModel(artistId, artist, num_albums, num_tracks));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return artistList;
    }
}