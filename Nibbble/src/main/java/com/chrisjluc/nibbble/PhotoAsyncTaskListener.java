package com.chrisjluc.nibbble;

/**
 * Created by chrisjluc on 2/4/2014.
 */
public interface PhotoAsyncTaskListener {
    public void onDownloadComplete();

    public void toastInvalidUsername();

    public void toastError();
}
