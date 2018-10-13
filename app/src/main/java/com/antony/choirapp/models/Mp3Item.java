package com.antony.choirapp.models;

public class Mp3Item {

    private String mSongName;
    private String mAddedUser;
    private String path;

    public Mp3Item() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getmSongName() {
        return mSongName;
    }

    public void setmSongName(String mSongName) {
        this.mSongName = mSongName;
    }

    public String getmAddedUser() {
        return mAddedUser;
    }

    public void setmAddedUser(String mAddedUser) {
        this.mAddedUser = mAddedUser;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }

        if (!Mp3Item.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Mp3Item other = (Mp3Item) obj;
        return this.getmSongName().equals(other.getmSongName()) && this.getmAddedUser().equals(other.getmAddedUser());
    }
}
