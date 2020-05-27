package com.github.hanavan99.netcopy.net;

public interface INetworkCallback {

	public void transferStarted();

	public void transferEnded();

	public void messageLogged(String message);

	public void progressUpdated(int cur, int max);

	public void fileProgressUpdated(long cur, long max);

	public void settingsChanged(boolean allowClientChanges, boolean createMissingFolders, boolean purgeDirectory, boolean transferNonexistingFiles, boolean transferExistingFiles, int filePreferenceMode);

}
