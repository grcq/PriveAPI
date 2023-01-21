package cf.grcq.processor.database;

public enum Type {

    MONGODB(null, null), MYSQL(null, null);

    private String uri;
    private String database;

    Type(String uri, String database) {
        this.uri = uri;
        this.database = database;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
