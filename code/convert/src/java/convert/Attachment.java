package convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

public class Attachment {

    private final HttpServletResponse response;

    private final String mimeType;

    private final String fileName;

    public Attachment(final HttpServletResponse response, final String mimeType, final String fileName) {
        this.response = response;
        this.mimeType = mimeType;
        this.fileName = fileName;
    }

    public void send(final byte[] data) {
        response.setContentType(mimeType);
        response.setContentLength(data.length);
        // see http://www.ietf.org/rfc/rfc2183.txt
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        // Workaround for IE bug, http://support.microsoft.com/kb/323308
        response.setHeader("Cache-Control", "max-age=0");
        response.setHeader("Pragma", "public");
        try {
            final OutputStream outputStream = response.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(final ByteArrayOutputStream baos) {
        send(baos.toByteArray());
    }

    public void send(final String string) {
        try {
            send(string.getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(final StringWriter stringWriter) {
        send(stringWriter.toString());
    }

}
