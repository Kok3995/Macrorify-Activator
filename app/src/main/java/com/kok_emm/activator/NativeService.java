package com.kok_emm.activator;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NativeService {
    public void start(Context context) throws Exception {
        String abi = readCmd("getprop ro.product.cpu.abi");
        if (abi == null)
            throw new Exception("Cannot get device abi");

        int resourceId = 0;

        switch (abi.replace(System.lineSeparator(), "")) {
            case "arm64-v8a": resourceId = R.raw.minitouch_arm64; break;
            case "armeabi-v7a": resourceId = R.raw.minitouch_arm32; break;
            case "x86": resourceId = R.raw.minitouch_x86; break;
            case "x86_64": resourceId = R.raw.minitouch_x86_64; break;
        }

        if (resourceId == 0)
            throw new Exception("Invalid Resource");

        File path = new File(context.getFilesDir(), "minitouch");
        path.createNewFile();

        try (FileOutputStream fileOutputStream = new FileOutputStream(path); InputStream is = context.getResources().openRawResource(resourceId)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        execCmd(new String[] { "cp -r " + path.getAbsolutePath() + " /data/local/tmp/minitouch", "chmod 777 /data/local/tmp/minitouch", "/data/local/tmp/minitouch" }, 1000);
    }

    public void stop() {
        killProcess("/data/local/tmp/minitouch");
    }

    private void execCmd(String[] cmds, int wait) {
        try {
            Process process = Runtime.getRuntime ().exec ("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream ());

            for (String cmd : cmds) os.writeBytes (cmd + "\n");

            os.writeBytes ("exit\n");

            os.flush ();
            os.close ();

            if (wait > 0)
                Thread.sleep(wait);
            else process.waitFor();
        } catch (IOException | InterruptedException e) {}
    }

    public boolean isServiceRunning() {
        String ps = readCmd("su -c ps");

        return ps != null && ps.contains("minitouch");
    }

    private String readCmd(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            return output.toString();
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private void killProcess (String name) {
        String line = readCmd("su -c ps | grep " + name);

        if (line == null)
            return;

        String[] data = line.split("\\s+");

        if (name.equals (data[8]))
            execCmd(new String[] { "kill -9 " + data[1] }, 0);
    }
}
