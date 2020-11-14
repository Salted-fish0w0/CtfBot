package org.hackbug.saltedfish.fishbot.ctf.http;

import org.hackbug.saltedfish.fishbot.ctf.main.BotHolder;
import org.hackbug.saltedfish.fishbot.ctf.user.CtfUser;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.content.ContentType;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpServer extends NanoHTTPD {
	private final Map<String, File> tempDownloadMap = new HashMap<>();
	private final String host;

	public HttpServer(int port) {
		super(port);
		host = "http://" + BotHolder.getConfig().getString("host") + ":" + port;

		this.setHTTPHandler(session -> {
			String originalContentType = session.getHeaders().get("content-type");
			ContentType ct = new ContentType(session.getHeaders().get("content-type")).tryUTF8();
			session.getHeaders().put("content-type", ct.getContentTypeHeader());

			try {
				switch (session.getUri()) {
					case "/upload":
						if (session.getMethod() != Method.POST) {
							return Response.newFixedLengthResponse(Status.FORBIDDEN, MIME_PLAINTEXT, "上传文件请从FishBot给的方式来操作！");
						}

						Map<String, String> header = new HashMap<>(session.getHeaders());
						header.put("content-type", originalContentType);

						return acceptUpload(UUID.fromString(session.getParameters().get("id").get(0)), header, session.getInputStream(), originalContentType.split(";")[1].split("=")[1]);
					case "/":
						return Response.newFixedLengthResponse(Status.OK, MIME_HTML, ("<!DOCTYPE html>\n" +
								"<html lang=\"en\">\n" +
								"<head>\n" +
								"    <meta charset=\"UTF-8\">\n" +
								"    <title>Writeup上传</title>\n" +
								"</head>\n" +
								"<body>\n" +
								"<form id=\"upload-form\" action=\"upload?id=%ID%\" method=\"post\" enctype=\"multipart/form-data\" >\n" +
								"         <input type=\"file\" id=\"upload\" name=\"upload\" /> <br/>\n" +
								"    　　　<input type=\"submit\" value=\"Upload\" />\n" +
								"</form>\n" +
								"</body>\n" +
								"</html>").replaceAll("%ID%", session.getParameters().get("id").get(0)));
					case "/download":
						// application/octet-stream
						return provideDownload(UUID.fromString(session.getParameters().get("id").get(0)));
					default:
						if (tempDownloadMap.containsKey(session.getUri())) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							try (FileInputStream fis = new FileInputStream(tempDownloadMap.get(session.getUri()))) {
								IOUtils.copy(fis, baos);
								tempDownloadMap.remove(session.getUri());
							} catch (Exception e) {
								e.printStackTrace();
								return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "出问题了！");
							}

							return Response.newFixedLengthResponse(Status.OK, "applicaton/octet-stream", baos.toByteArray());
						} else {
							return Response.newFixedLengthResponse(Status.FORBIDDEN, MIME_PLAINTEXT, "不允许乱翻人家的东西！");
						}
				}
			} catch (IllegalArgumentException e) {
				return Response.newFixedLengthResponse(Status.BAD_REQUEST, MIME_PLAINTEXT, "请找FishBot重新获取一个文件操作链接");
			}
		});
	}

	private Response acceptUpload(UUID session, Map<String, String> header, InputStream inputStream, String boundary) {
		try {
			CtfUser cu = BotHolder.getMysql().getUserFromFileUuid(session);
			if (cu == null) {
				return Response.newFixedLengthResponse(Status.FORBIDDEN, MIME_PLAINTEXT, "请找FishBot重新获取一个上传链接");
			}

			ServletFileUpload servlet = new ServletFileUpload(new DiskFileItemFactory());
			servlet.setHeaderEncoding("UTF-8");
			List<FileItem> fileItems = servlet.parseRequest(new RequestContext() {
				@Override
				public String getCharacterEncoding() {
					return "UTF-8";
				}

				@Override
				public String getContentType() {
					return header.get("content-type");
				}

				@Override
				public int getContentLength() {
					return Integer.parseInt(header.get("content-length"));
				}

				@Override
				public InputStream getInputStream() {
					return inputStream;
				}
			});

			for (FileItem fileItem : fileItems) {
				if (fileItem.isFormField()) {
					continue;
				}

				File f = new File(new File(BotHolder.getConfig().getString("file-upload-folder")), cu.getQq() + "_" + fileItem.getName());
				fileItem.write(f);
			}

			return Response.newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, "上传完成");
		} catch (Exception e) {
			e.printStackTrace();
			return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "出问题了！");
		}
	}
	private Response provideDownload(UUID session) {
		try {
			String fileName = BotHolder.getMysql().getFile(session);
			if (fileName == null) {
				return Response.newFixedLengthResponse(Status.FORBIDDEN, MIME_PLAINTEXT, "请找FishBot重新获取下载链接");
			}
			File targetFile = new File(fileName);
			tempDownloadMap.put("/download/" + targetFile.getName(), targetFile);

			Response redirectResponse = Response.newFixedLengthResponse(Status.FOUND, MIME_HTML, "");
			redirectResponse.addHeader("Location", host + "/download/" + targetFile.getName());
			return redirectResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "出问题了！");
		}
	}
}
