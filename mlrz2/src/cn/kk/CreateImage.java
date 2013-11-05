package cn.kk;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class CreateImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置缓存参数 
		response.setDateHeader("Expires", -1);
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setContentType("image/jpeg");
		
		String url = request.getHeader("REFERER"); 
		if(url==null){
			ImageIO.write(ImageIO.read(new File(srcImg)), "jpg", response.getOutputStream());
			return;
		}
		qq = getQq(url);
		System.out.println(qq);
		try {
			//根据QQ号初始化数据
			parseJson(getJsonString());
		} catch (Exception e) {
			ImageIO.write(ImageIO.read(new File(srcImg)), "jpg", response.getOutputStream());
			e.printStackTrace();
		}
		
		ImageIO.write(createImage(), "jpg", response.getOutputStream());
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	/**QQ号 */
	private String qq;
	/**QQ头像地址 */
	private String portrait;
	/**QQ昵称 */
	private String nickname;
	/**源图片地址 */
	private String srcImg = "/static/src.jpg";
	
	@Override
	public void init() throws ServletException {
		super.init();
		srcImg = this.getServletContext().getRealPath("/").replace("\\", "/")+srcImg;
		System.out.println(srcImg);
	}
	/**
	 * 根据URL获取QQ号码
	 * @param url
	 * @return
	 */
	public String  getQq(String url){
		String qq = "530427968";
		Matcher m = Pattern.compile("[0-9]{6,11}").matcher(url);
		if(m.find()){
			qq = m.group(0);
		}
		return qq;
	}
	

	private  String getJsonString() throws Exception {
		URL url = new URL("http://base.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins="+qq);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		InputStream inputStream = connection.getInputStream();
		//对应的字符编码转换
		Reader reader = new InputStreamReader(inputStream, "GB2312");
		BufferedReader bufferedReader = new BufferedReader(reader);
		String str = null;
		StringBuffer sb = new StringBuffer();
		while ((str = bufferedReader.readLine()) != null) {
			sb.append(str);
		}
		reader.close();
		connection.disconnect();
		return sb.toString().substring(sb.toString().indexOf("(")+1,sb.toString().length()-1);
	}
	
	/**
	 * 解析JSON数据
	 * @param jsonStr
	 * @throws Exception
	 */
	private  void parseJson(String jsonStr) throws Exception {
		JSONObject jsonObject =JSONObject.fromObject(jsonStr);  
		qq = jsonStr.substring(2,jsonStr.indexOf("\":"));
		JSONArray childs= jsonObject.getJSONArray(qq);
		portrait = (String)childs.get(0);
		nickname = (String)childs.get(6);
	}
	
	/**
	 * 绘制图片
	 * @return
	 * @throws IOException
	 */
	private BufferedImage createImage() throws IOException{
		BufferedImage img = ImageIO.read(new File(srcImg));
		//开始绘图
		Graphics g = img.getGraphics();
		//设置字体属性
		g.setColor(Color.RED);
		g.setFont(new Font("宋体",Font.BOLD, 14));
		//绘制QQ号
		g.drawString(qq, 70,145);
		//得到头像并绘制
		URL url = new URL(portrait);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        BufferedImage image = ImageIO.read(connection.getInputStream());
        g.drawImage(image, 70, 160, null);
        //绘制QQ昵称
        g.drawString(nickname, 70, 123);
		return img;
	}	
}
