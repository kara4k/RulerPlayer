package com.kara4k.moozic.database;


public class DbSchemes {




    public static final class SearchTracks {
        public static final String NAME = "searched_tracks";


        public static final class Cols {
            public static final String NAME = "file_name";
            public static final String TRACK_FILE = "file";
            public static final String FILE_PATH = "file_path";
            public static final String TRACK_NAME = "track_name";
            public static final String TRACK_ARTIST = "track_artist";
            public static final String DURATION_MS = "duration_ms";
            public static final String DURATION = "duration";
            public static final String EXTENSION = "extension";
            public static final String DATE = "date";
            public static final String BITRATE = "bitrate";
            public static final String IS_RADIO = "is_radio";
            public static final String POSITION = "position";
        }
    }

}
