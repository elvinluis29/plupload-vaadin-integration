package com.ez.plupload;

enum Option {
    RESIZE("resize"),
    FILTERS("filters"),
    RUNTIMES("runtimes"),
    MULTIPART("multipart"),
    CHUNK_SIZE("chunk_size"),
    MAX_RETRIES("max_retries"),
    MAX_FILE_SIZE("max_file_size"),
    MULTI_SELECTION("multi_selection"),
    PREVENT_DUPLICATES("prevent_duplicates");

    private final String name;

    private Option(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}