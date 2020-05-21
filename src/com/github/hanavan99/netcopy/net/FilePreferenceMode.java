package com.github.hanavan99.netcopy.net;

public enum FilePreferenceMode {

	TRANSFER_ALL("Transfer all files"),
	TRANSFER_LARGER("Transfer files that are larger"),
	TRANSFER_EQUAL("Transfer files that are equal"),
	TRANSFER_SMALLER("Transfer files that are smaller"),
	TRANSFER_NEWER("Transfer files with newer last modified date"),
	TRANSFER_SAME_AND_NEWER("Transfer files with same last modified date and newer"),
	TRANSFER_SAME("Transfer files with same last modified date"),
	TRANSFER_SAME_AND_OLDER("Transfer files with same last modified date and older"),
	TRANSFER_OLDER("Transfer files with older last modified date");

	private final String text;

	private FilePreferenceMode(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

}
