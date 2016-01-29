package practice.hanchen.autoscrolllyrics;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
	static final String LOG_TAG = "MainActivity";
	private LyricsManager lyricsManager;
	private MediaPlayer mediaPlayer;
	private ListView listViewLyrics;
	private Timer timer;
	private int currentLyricsPosition;
	private int absFirstLyricsChildPosition;
	private int centerPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		InputStream inputStream;
		AssetManager am = getAssets();
		try {
			inputStream = am.open("lyrics.xml");
			lyricsManager = new LyricsManager();
			lyricsManager.parseLyricsAndTime(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayAdapter<String> lyricsAdatper =
				new ArrayAdapter<String>(this, R.layout.layout_lyrics_line, R.id.label_lyrics, lyricsManager.getLyrics());
		listViewLyrics = (ListView) findViewById(R.id.listview_lyrics);
		listViewLyrics.setAdapter(lyricsAdatper);
		playSongAndSyncLyrics();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void playSongAndSyncLyrics() {
		if (mediaPlayer == null) {
			mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.notyourkindpeople);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.start();
			timer = new Timer();
			timer.scheduleAtFixedRate(new MusicTimerTask(), 0, 300);
		}

		if(!mediaPlayer.isPlaying()) {
			absFirstLyricsChildPosition = 0;
			currentLyricsPosition = 0;
			listViewLyrics.setSelection(0);
			mediaPlayer.start();
			timer = new Timer();
			timer.scheduleAtFixedRate(new MusicTimerTask(), 0, 300);
		}
	}

	private class MusicTimerTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mediaPlayer != null && mediaPlayer.isPlaying()) {
						if (lyricsManager.shouldChangeLine(mediaPlayer, currentLyricsPosition + absFirstLyricsChildPosition)) {
							if (listViewLyrics.getChildCount() > 0) {
								if (centerPosition == 0) {
									centerPosition = listViewLyrics.getChildCount() / 2;
								}

								if (currentLyricsPosition + 1 >= centerPosition) {
									if (listViewLyrics.getLastVisiblePosition() == listViewLyrics.getCount() - 1) {
										currentLyricsPosition++;
										changeLine(currentLyricsPosition);
									} else {
										absFirstLyricsChildPosition++;
										changeLine(currentLyricsPosition + 1);
										listViewLyrics.setSelection(absFirstLyricsChildPosition);
									}
								} else {
									currentLyricsPosition++;
									changeLine(currentLyricsPosition);
								}
							}
						}
					} else {
						playSongAndSyncLyrics();
					}
				}
			});
		}
	}

	private void changeLine(int position) {
		View viewLyrics;
		TextView labelLyrics;

		if (position > 0) {
			viewLyrics = listViewLyrics.getChildAt(position - 1);
			labelLyrics = (TextView) viewLyrics.findViewById(R.id.label_lyrics);
			labelLyrics.setTextColor(Color.BLACK);
		}

		if (position < listViewLyrics.getChildCount() - 1) {
			viewLyrics = listViewLyrics.getChildAt(position);
			labelLyrics = (TextView) viewLyrics.findViewById(R.id.label_lyrics);
			labelLyrics.setTextColor(Color.BLUE);
		}
	}
}
