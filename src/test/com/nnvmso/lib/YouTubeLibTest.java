package test.com.nnvmso.lib;

import static org.junit.Assert.*;
import org.junit.Test;

import com.nnvmso.lib.YouTubeLib;

public class YouTubeLibTest {
	
	private String[] variousYouTubeChannelUrls = {
			"http://www.youtube.com/user/CNETTV#g/u",
			"http://www.youtube.com/user/CNETTV#p/u",
			"http://www.youtube.com/profile?user=CNETTV"
	};
	private String unifiedYouTubeChannelUrl = "http://www.youtube.com/user/cnettv";
	
	private String[] variousYouTubePlaylistUrls = {
			"http://www.youtube.com/user/view_play_list?p=37A52BDCC59D0BF2",
			
			"http://www.youtube.com/user/NASAtelevision#g/c/37A52BDCC59D0BF2",
			"http://www.youtube.com/user/NASAtelevision#grid/user/37A52BDCC59D0BF2",
			"http://www.youtube.com/user/NASAtelevision#p/c/37A52BDCC59D0BF2",
			"http://www.youtube.com/user/NASAtelevision#p/c/37A52BDCC59D0BF2/2/2zihywXgyOI",
			
			"http://www.youtube.com/profile?user=NASAtelevision#g/c/37A52BDCC59D0BF2",
			"http://www.youtube.com/profile?user=NASAtelevision#grid/user/37A52BDCC59D0BF2",
			"http://www.youtube.com/profile?user=NASAtelevision#p/c/37A52BDCC59D0BF2",
			"http://www.youtube.com/profile?user=NASAtelevision#p/c/37A52BDCC59D0BF2/2/2zihywXgyOI",
			
			"http://www.youtube.com/watch?v=2zihywXgyOI&p=37A52BDCC59D0BF2",
			"http://www.youtube.com/watch?v=2zihywXgyOI&list=PL37A52BDCC59D0BF2",
			"http://www.youtube.com/watch?v=2zihywXgyOI&playnext=1&list=PL37A52BDCC59D0BF2",
			"http://www.youtube.com/watch?v=2zihywXgyOI&playnext=1&list=PL37A52BDCC59D0BF2&feature=list_related",
			
			"http://www.youtube.com/playlist?p=37A52BDCC59D0BF2",
			"https://www.youtube.com/my_playlist?p=37A52BDCC59D0BF2",
	};
	private String unifiedYouTubePlaylistUrl = "http://www.youtube.com/view_play_list?p=37a52bdcc59d0bf2";
	
	@Test
	public void testFormatCheck() {
		for (String origin : variousYouTubeChannelUrls) {
			String checked = YouTubeLib.formatCheck(origin);
			assertEquals(origin, unifiedYouTubeChannelUrl, checked);
		}
		for (String origin : variousYouTubePlaylistUrls) {
			String checked = YouTubeLib.formatCheck(origin);
			assertEquals(origin, unifiedYouTubePlaylistUrl, checked);
		}
	}
	
}
