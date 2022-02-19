import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;


public class Application {
    public static void main(String... args) {

        System.out.println(Configuration.instance.pathToFilesForEncryption);

        File folder = new File(Configuration.instance.pathToFilesForEncryption);
        File[] listOfFiles = folder.listFiles();


        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println(listOfFiles[i].getName());

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println(listOfFiles[i].getName());
            }
        }
        System.out.println();



        String originalString = "smart_home.txt";

        String encryptedString = AES256.encrypt(originalString);
        String decryptedString = AES256.decrypt(encryptedString);

        System.out.println(originalString);
        System.out.println(encryptedString);
        System.out.println(decryptedString);

        Application application = new Application();
        application.generateKeys();

        /*Scanner scanner = new Scanner(System.in);

        System.out.print("[insurance]                  : ");
        String insurance = scanner.nextLine();
        System.out.println(insurance);

        System.out.print("[tax]                        : ");
        double tax = scanner.nextDouble();
        System.out.println(tax);

        System.out.print("[defaultParkingSpaceID]      : ");
        int defaultParkingSpaceID = scanner.nextInt();
        System.out.println(defaultParkingSpaceID);*/


    }

    private String adjustTo64(String string) {
        return switch (string.length()) {
            case 62 -> "00" + string;
            case 63 -> "0" + string;
            case 64 -> string;
            default -> throw new IllegalArgumentException("not a valid key | " + string);
        };
    }

    public String bytesToHex(byte[] bytes) {
        byte[] hexArray = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
        byte[] hexChars = new byte[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public void generateKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecGeneratorParameterSpecification = new ECGenParameterSpec(Configuration.instance.EC_GENERATOR_SPECIFICATION_ALGORITHM);

            keyPairGenerator.initialize(ecGeneratorParameterSpecification);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;
            String result = adjustTo64(ecPrivateKey.getS().toString(16)).toUpperCase();
            System.out.println("s[" + result.length() + "] (stored in wallet) | " + result);
            ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
            ECPoint ecPoint = ecPublicKey.getW();
            String sx = adjustTo64(ecPoint.getAffineX().toString(16)).toUpperCase();
            String sy = adjustTo64(ecPoint.getAffineY().toString(16)).toUpperCase();
            String bcPublicKey = "04" + sx + sy;
            System.out.println("bcPublicKey              | " + bcPublicKey);

            MessageDigest shaMessageDigest01 = MessageDigest.getInstance(Configuration.instance.MESSAGE_DIGEST_SHA_ALGORITHM);
            byte[] resultSHAMessageDigest = shaMessageDigest01.digest(bcPublicKey.getBytes(StandardCharsets.UTF_8));
            System.out.println("shaMessageDigest01       | " + bytesToHex(resultSHAMessageDigest).toUpperCase());

            MessageDigest md5MessageDigest01 = MessageDigest.getInstance(Configuration.instance.MESSAGE_DIGEST_MD5_ALGORITHM);
            byte[] md5MessageDigest02 = md5MessageDigest01.digest(resultSHAMessageDigest);
            byte[] md5MessageDigest03 = new byte[md5MessageDigest02.length + 1];
            md5MessageDigest03[0] = 0;
            System.arraycopy(md5MessageDigest02, 0, md5MessageDigest03, 1, md5MessageDigest02.length);
            System.out.println("md5MessageDigest01       | " + bytesToHex(md5MessageDigest03).toUpperCase());

            byte[] shaMessageDigest02 = shaMessageDigest01.digest(md5MessageDigest03);
            System.out.println("shaMessageDigest02       | " + bytesToHex(shaMessageDigest02).toUpperCase());

            byte[] shaMessageDigest03 = shaMessageDigest01.digest(shaMessageDigest02);
            System.out.println("shaMessageDigest03       | " + bytesToHex(shaMessageDigest03).toUpperCase());

            byte[] temp = new byte[25];
            System.arraycopy(md5MessageDigest02, 0, temp, 0, md5MessageDigest02.length);
            System.arraycopy(shaMessageDigest03, 0, temp, 20, 5);
            System.out.println("transaction address      | " + Base58.encode(temp));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}