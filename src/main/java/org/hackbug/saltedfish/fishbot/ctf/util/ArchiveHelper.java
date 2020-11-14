package org.hackbug.saltedfish.fishbot.ctf.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;

public class ArchiveHelper {
	public static void buildTar(File file, File target) throws IOException {
		if (!file.exists()) {
			return;
		}
		if (!target.exists()) {
			target.createNewFile();
		}
		try (TarArchiveOutputStream taos = new TarArchiveOutputStream(new FileOutputStream(target))) {
			archive(file, taos, file.getName());
		}
	}
	private static void archive(File target, TarArchiveOutputStream taos, String basePath) throws IOException {
		if (target.isDirectory()) {
			archiveDir(target, taos, basePath);
		} else {
			archiveFile(target, taos, basePath);
		}
	}

	private static void archiveDir(File file, TarArchiveOutputStream tos, String basePath) throws IOException {
		File[] listFiles = file.listFiles();
		for(File fi : listFiles){
			if(fi.isDirectory()){
				archiveDir(fi, tos, basePath + File.separator + fi.getName());
			}else{
				archiveFile(fi, tos, basePath);
			}
		}
	}

	private static void archiveFile(File target, TarArchiveOutputStream taos, String basePath) throws IOException {
		TarArchiveEntry tEntry = new TarArchiveEntry(basePath + File.separator + target.getName());
		tEntry.setSize(target.length());

		taos.putArchiveEntry(tEntry);

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(target));

		byte[] buffer = new byte[1024];
		int read = -1;
		while((read = bis.read(buffer)) != -1) {
			taos.write(buffer, 0 , read);
		}
		bis.close();
		taos.closeArchiveEntry();
	}
}
