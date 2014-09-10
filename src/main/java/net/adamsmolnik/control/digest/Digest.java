package net.adamsmolnik.control.digest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import net.adamsmolnik.entity.EntityReference;
import net.adamsmolnik.exceptions.ServiceException;
import net.adamsmolnik.provider.EntityProvider;

/**
 * @author ASmolnik
 *
 */
@Dependent
public class Digest {

    @Inject
    private EntityProvider entityProvider;

    public String doDigest(String algorithm, String objectKey) {
        try (InputStream is = getInputStream(objectKey)) {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            String digest = DatatypeConverter.printHexBinary(md.digest(getBytes(is)));
            return digest;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new ServiceException(e);
        }
    }

    private InputStream getInputStream(String objectKey) throws IOException {
        String localPrefix = "local:";
        if (objectKey.startsWith(localPrefix)) {
            Path path = Paths.get(objectKey.substring(localPrefix.length()));
            return Files.newInputStream(path);
        }
        return entityProvider.getEntity(new EntityReference(objectKey)).getInputStream();
    }

    private static byte[] getBytes(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead = 0;
        try {
            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return buffer.toByteArray();

    }
}
