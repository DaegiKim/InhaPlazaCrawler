import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class InhaPlazaCrawler extends Thread {
    //게시글 URL
    private static final String LOGIN_REQUEST_FORM_DATA = "dest=http%3A%2F%2Fwww.inha.ac.kr&uid={uid}&pwd={pwd}";

    private static final String LOGIN_URL = "https://www.inha.ac.kr/common/asp/login/loginProcess.asp";
    private static final String REQUEST_URL = "http://www.inha.ac.kr/plaza/talktalk/talktalkView.asp?pBType=1&pIdx={index}";

    private String id;
    private String pw;
    private String cookie;
    private Queue<Integer> queue;

    public InhaPlazaCrawler(String id, String pw, Queue<Integer> queue) {
        this.id = id;
        this.pw = pw;
        this.queue = queue;
    }

    @Override
    public void run() {
        getCookie();

        while (!this.queue.isEmpty()) {
            Integer index = this.queue.poll();
            getHtml(index);
        }
    }

    private void getHtml(int index) {

        HttpURLConnection hConnection = null;
        InputStream is = null;
        BufferedReader in = null;
        BufferedWriter out = null;

        try{
            //TODO: getOutputStream, getInputStream 을 한번으로 줄일 수 있음?
            URL url = new URL(REQUEST_URL.replace("{index}", String.valueOf(index)));
            hConnection = (HttpURLConnection)url.openConnection();
            hConnection.setInstanceFollowRedirects(false);
            hConnection.setDoOutput(true);
            hConnection.setRequestMethod("GET");
            hConnection.addRequestProperty("Cookie", cookie);

            if((is = hConnection.getInputStream()) != null)
            {
                System.out.print(this.getName() + "-");
                System.out.println(index + "-" + hConnection.getResponseCode() + "-" + hConnection.getResponseMessage());
                in = new BufferedReader(new InputStreamReader(is, Charset.forName("EUC-KR")));
                out= new BufferedWriter( new FileWriter(String.format("data/%d.html",index)));
                String readLine;

                while((readLine=in.readLine()) != null)
                {
                    out.write(readLine+"\n");
                    out.flush();
                }
            }
            else {
                System.err.println("Connection failed...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            hConnection.disconnect();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getCookie() {
        String uid = new String(Base64.encodeBase64(this.id.getBytes()));
        String pwd = new String(Base64.encodeBase64(this.pw.getBytes()));

        HttpURLConnection hConnection = null;
        PrintStream ps = null;
        InputStream is = null;
        BufferedReader in = null;

        try{
            URL url = new URL(LOGIN_URL);
            hConnection = (HttpURLConnection)url.openConnection();
            hConnection.setInstanceFollowRedirects(false);
            hConnection.setDoOutput(true);
            hConnection.setRequestMethod("POST");
            ps = new PrintStream(hConnection.getOutputStream());

            //인코딩된 uid, pwd 로 문자열 replace
            ps.print(LOGIN_REQUEST_FORM_DATA.replace("{uid}", uid).replace("{pwd}", pwd));

            if((is = hConnection.getInputStream()) != null)
            {
                Map<String, List<String>> headerFields = hConnection.getHeaderFields();
                for(String key : headerFields.keySet()) {
                    List<String> values = headerFields.get(key);

                    if("Set-Cookie".equals(key)) {
                        for (String v : values) {
                            if(v.startsWith("user%5Finfo")) {
                                String[] split = v.split(";");
                                if(split[0].length()>0) {
                                    this.cookie = split[0];
                                }
                            }
                        }
                    }
                }
            }
            else {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            //스트림 닫기
            hConnection.disconnect();
            ps.close();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
