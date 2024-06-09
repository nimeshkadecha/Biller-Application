package com.nimeshkadecha.myapplication;

import static com.nimeshkadecha.myapplication.Gemini_Home.business_JO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class Gemini_chat extends AppCompatActivity {

	public static final String SHARED_PREFS = "sharedPrefs";

	DBManager dbManager = new DBManager(this);

	int sellerId;
	private RecyclerView recyclerView;
	private MessageAdapter messageAdapter;
	private List<ChatMessage> messageList;
	private EditText editTextMessage;
	private Button buttonSend;
	ChatFutures chat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gemini_chat);


		//      WORKING WITH TOOLBAR Starts ----------------------------------------------------------------
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
		//          menu Button ----------------------------------------------------------------------------
		ImageView menuclick = findViewById(R.id.Menu);
//          Keeping MENUE Invisible
		menuclick.setVisibility(View.INVISIBLE);

		//      WORKING WITH TOOLBAR Ends ----------------------------------------------------------------

		Bundle bundle = getIntent().getExtras();
		String email = bundle.getString("seller");
		String Query_type = bundle.getString("type");

		sellerId = dbManager.get_userId(email);

		String GeminiInitialString = "";

		assert Query_type != null;
		switch (Query_type) {
			case "business":
				GeminiInitialString += "Here is all data of my business.\n\n" + String.valueOf(business_JO) + "\n\nAnswer all follow up question as short as possible (in 2-3 sentences) and using only this data";
				break;
			case "stock":
				GeminiInitialString += "Here is all data of my business about stock and products.\n\n" + String.valueOf(business_JO) + "\n\nAnswer all follow up question as short as possible (in 2-3 sentences) and using only this data";
				break;
			case "customer":
				GeminiInitialString += "Here is all data of my business about customers.\n\n" + String.valueOf(business_JO) + "\n\nAnswer all follow up question as short as possible (in 2-3 sentences) and using only this data";
				break;
		}


		recyclerView = findViewById(R.id.recyclerView_gemini);
		editTextMessage = findViewById(R.id.editTextMessage);
		buttonSend = findViewById(R.id.buttonSend);

		messageList = new ArrayList<>();
		messageAdapter = new MessageAdapter(messageList);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(messageAdapter);


//		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//			@Override
//			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//				super.onScrolled(recyclerView, dx, dy);
//				Log.d("ENimesh","scroled !");
//				LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//				if (layoutManager != null && layoutManager.findFirstCompletelyVisibleItemPosition() == 0 && dy < 0) {
//					// User has scrolled to the top
//					if (messageList.isEmpty() || !isToday(messageList.get(0).getTimestamp())) {
//						// Load older chats from the previous day
//						loadPreviousDayChats();
//					} else {
//						// Load older chats for today
//						loadOlderChats();
//					}
//				}
//			}
//		});

			new LoadMessagesTask(getApplicationContext(), sellerId, messageList, messageAdapter).execute();


		// The Gemini 1.5 models are versatile and work with multi-turn conversations (like chat)
		GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash", dbManager.getApiKey(email));
		GenerativeModelFutures model = GenerativeModelFutures.from(gm);
// Initialize the chat
		chat = model.startChat();

		buttonSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String messageText = editTextMessage.getText().toString();
				if (!messageText.isEmpty()) {
					// Add user message to the list and database
					ChatMessage userMessage = new ChatMessage(messageText, true, sellerId);
					saveMessage(userMessage);

					new SendMessageTask().execute(messageText);

					// Clear the input field
					editTextMessage.setText("");
				}
			}
		});

		if (!Query_type.equals("continue")) {

			// initial user message with data
			Content.Builder userContentBuilder3 = new Content.Builder();
			userContentBuilder3.setRole("user");
			userContentBuilder3.addText(GeminiInitialString);
			Content userContent3 = userContentBuilder3.build();

// Send the message
			ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userContent3);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
					@Override
					public void onSuccess(GenerateContentResponse result) {
						String resultText = result.getText();
						System.out.println(resultText);
						Toast.makeText(Gemini_chat.this, "Your app data has been successfully synced with GEMINI.", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(Throwable t) {
						t.printStackTrace();
					}
				}, getMainExecutor());
			}


		}

		findViewById(R.id.fab_clear_chat).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(Gemini_chat.this, "Long click to delete all chat", Toast.LENGTH_SHORT).show();
			}
		});

		findViewById(R.id.fab_clear_chat).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				if (dbManager.clearChat(String.valueOf(sellerId))) {
					messageList.clear();
					messageAdapter.notifyDataSetChanged();
					Toast.makeText(Gemini_chat.this, "Clear chat", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});

	}


	private class SendMessageTask extends AsyncTask<String, Void, String> {
		private String userMessage;

		@Override
		protected String doInBackground(String... params) {
			userMessage = params[0];
			final String[] responseAI = new String[1];
			final CountDownLatch latch = new CountDownLatch(1); // Initialize CountDownLatch with count 1

			Content.Builder userContentBuilder3 = new Content.Builder();
			userContentBuilder3.setRole("user");
			userContentBuilder3.addText(userMessage);
			Content userContent3 = userContentBuilder3.build();

			ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userContent3);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
					@Override
					public void onSuccess(GenerateContentResponse result) {
						String resultText = result.getText();
						System.out.println(resultText);
						Log.d("ENimesh", "data from GEMINI = " + resultText);
						responseAI[0] = resultText;
						latch.countDown(); // Decrement the count of the latch, releasing all waiting threads
					}

					@Override
					public void onFailure(Throwable t) {
						t.printStackTrace();
						latch.countDown(); // Decrement the count of the latch, releasing all waiting threads
					}
				}, getMainExecutor());
			}

			try {
				latch.await(); // Wait until the response is received
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return responseAI[0];
		}

		@Override
		protected void onPostExecute(String aiResponse) {
			if (aiResponse != null) {
				// Add AI response to the list and database
				ChatMessage aiMessage = new ChatMessage(aiResponse, false, sellerId);
				saveMessage(aiMessage);
			} else {
				Toast.makeText(Gemini_chat.this, "Failed to get response from API", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public class LoadMessagesTask extends AsyncTask<Void, Void, List<ChatMessage>> {
		private DBManager dbManager;
		private WeakReference<Context> contextRef;
		private WeakReference<MessageAdapter> adapterRef;
		private List<ChatMessage> messageList;
		private int sellerId;

		public LoadMessagesTask(Context context, int sellerId, List<ChatMessage> messageList, MessageAdapter adapter) {
			this.contextRef = new WeakReference<>(context);
			this.sellerId = sellerId;
			this.messageList = messageList;
			this.adapterRef = new WeakReference<>(adapter);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Context context = contextRef.get();
			if (context != null) {
				dbManager = new DBManager(context);
			}
		}

		@Override
		protected List<ChatMessage> doInBackground(Void... voids) {
			if (dbManager != null) {
				return dbManager.getChatBySeller(sellerId);
			} else {
				return new ArrayList<>();
			}
		}

		@Override
		protected void onPostExecute(List<ChatMessage> chatMessages) {
			if (chatMessages != null && adapterRef.get() != null) {
				messageList.clear();
				messageList.addAll(chatMessages);
				adapterRef.get().notifyDataSetChanged();
				if (dbManager != null) {
					dbManager.close();
				}
			}
		}

		@Override
		protected void onCancelled() {
			if (dbManager != null) {
				dbManager.close();
			}
		}
	}


	@SuppressLint("StaticFieldLeak")
	private void saveMessage(ChatMessage message) {
		messageList.add(message);
		messageAdapter.notifyItemInserted(messageList.size() - 1);
		recyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the bottom

		new AsyncTask<ChatMessage, Void, Void>() {
			@Override
			protected Void doInBackground(ChatMessage... chatMessages) {

				dbManager.insertMessage(chatMessages[0]);
				return null;
			}
		}.execute(message);
	}

}