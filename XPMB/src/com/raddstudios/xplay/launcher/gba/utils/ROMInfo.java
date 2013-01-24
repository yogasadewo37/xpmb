package com.raddstudios.xplay.launcher.gba.utils;

import android.graphics.Bitmap;

public class ROMInfo {

	private Bitmap bmGameCover = null, bmGameBackground = null;
	private String strROMPath = null, strGameName = null, strGameCode = null,
			strGameCRC = null, strGameDescription = null,
			strGameRegions = null, strGameLanguages;

	public ROMInfo(String romPath, String gameCode, String gameCRC) {
		strROMPath = romPath;
		strGameCode = gameCode;
		strGameName = new java.io.File(romPath).getName();
		strGameCRC = gameCRC;
	}

	public String getROMPath() {
		return strROMPath;
	}

	public String getGameCode() {
		return strGameCode;
	}

	public void setGameRegions(String gameRegions) {
		strGameRegions = gameRegions;
	}

	public String getGameRegions() {
		return strGameRegions;
	}

	public void setGameLanguages(String gameLanguages) {
		strGameLanguages = gameLanguages;
	}

	public String getGameLanguages() {
		return strGameLanguages;
	}

	public void setGameBackground(Bitmap gameBackground){
		bmGameBackground = gameBackground;
	}
	
	public Bitmap getGameBackground(){
		return bmGameBackground;
	}
	
	public void setGameCover(Bitmap cover) {
		bmGameCover = cover;
	}

	public Bitmap getGameCover() {
		return bmGameCover;
	}

	public void setGameName(String gameName) {
		strGameName = gameName;
	}

	public String getGameName() {
		return strGameName;
	}

	public String getGameCRC() {
		return strGameCRC;
	}

	public void setGameDescription(String gameDescription) {
		strGameDescription = gameDescription;
	}

	public String getGameDescription() {
		return strGameDescription;
	}

}
