package com.nimeshkadecha.biller;

import static com.nimeshkadecha.biller.Gemini_Home.business_JO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

	private static final String SHARED_PREFS = "sharedPrefs";

	DBManager dbManager = new DBManager(this);

	int sellerId;
	private static RecyclerView recyclerView;
	private MessageAdapter messageAdapter;
	private List<ChatMessage> messageList;
	private EditText editTextMessage;
	ChatFutures chat;
	private String messageText;
	private int retryCount = 0;

	@SuppressLint("NotifyDataSetChanged")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gemini_chat);


		//      WORKING WITH TOOLBAR =====================================================================
//          Removing Suport bar / top line containing name
		Objects.requireNonNull(getSupportActionBar()).hide();
//          Keeping MENUE Invisible
		findViewById(R.id.Menu).setVisibility(View.INVISIBLE);

// =================================================================================================

		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		String email = bundle.getString("seller");
		String Query_type = bundle.getString("type");

		sellerId = dbManager.get_userId(email);

		String GeminiInitialString = "";

		assert Query_type != null;
		switch (Query_type) {
			case "business":
				GeminiInitialString += "Act as Chief Financial Analyst + Data Analysist + Data Scientest using this business data: \n\n" + business_JO + ".\n\n" +
												"Data Map :\n" +
												"1. prods: [ID: [Name, Category, Cost, TargetPrice, CurrentStock, GST%]]\n" +
												"2. custs: [ID: [Name, Phone]]\n" +
												"3. sales: [Date, BillNo, ProdID, CustID, Qty, SoldPrice, GST%]" +
												" Follow this decision tree:\n\n" +
												"1. For profits:\n" +
												"- Per Sale = (salesData.price × quantity) - (stockData.purchasePrice × quantity)\n" +
												"- Margin % = ((price - purchasePrice)/price)×100\n" +
												"- Total = Σ all salesData.subtotal - Σ (purchasePrice×quantity)\n" +
												"\n" +
												"2. GST Checks:\n" +
												"- For me my GST is TAX.\n" +
												"- Verify salesData.Gst matches productData.category's typical rate\n" +
												"- Flag deviations >±1% from category norms\n" +
												"\n" +
												"3. Inventory Analysis:\n" +
												"- Sales Velocity = total sold/(max_date - min_date in salesData)\n" +
												"- Stock Coverage = stockQuantityData.quantity / velocity\n" +
												"\n" +
												"4. Customer Value:\n" +
												"- Lifetime Spend = Σ subtotal by customerId\n" +
												"- Visit Frequency = salesData entries count per customerId\n" +
												"\n" +
												"Response MUST:\n" +
												"1. Convert ALL IDs → names immediately\n" +
												"2. Show 1-line formula context (e.g., \"60% margin = (50-20)/50\")\n" +
												"3. Highlight negative margins as LOSSES\n" +
												"4. Compare prices: stockData.sellingPrice vs salesData.price\n" +
												"5. 3 sentences max, bold** key figures\n" +
												"6. Simplest and sortest that contains only important details no explanation unless asked for";
				break;
			case "stock":
				GeminiInitialString += "Act as Chief stock Analyst + Data Analysist + Data Scientest using this Stock data: \n\n"+ business_JO + ".\n\n" +
												"Data Map :\n" +
												"1. prods: [ID: [Name, Category, Cost, TargetPrice, CurrentStock, GST%]]\n" +
												"2. sales: [Date, BillNo, ProdID, CustID, Qty, SoldPrice, GST%]" +
												" Follow this decision tree:\n\n" +
												"1. For profits:\n" +
												"- Per Sale = (salesData.price × quantity) - (stockData.purchasePrice × quantity)\n" +
												"- Margin % = ((price - purchasePrice)/price)×100\n" +
												"- Total = Σ all salesData.subtotal - Σ (purchasePrice×quantity)\n" +
												"\n" +
												"2. GST Checks:\n" +
												"- For me my GST is TAX.\n" +
												"- Verify salesData.Gst matches productData.category's typical rate\n" +
												"- Flag deviations >±1% from category norms\n" +
												"\n" +
												"3. Inventory Analysis:\n" +
												"- Sales Velocity = total sold/(max_date - min_date in salesData)\n" +
												"- Stock Coverage = stockQuantityData.quantity / velocity\n" +
												"\n" +
												"4. Customer Value:\n" +
												"- Lifetime Spend = Σ subtotal by customerId\n" +
												"- Visit Frequency = salesData entries count per customerId\n" +
												"\n" +
												"Response MUST:\n" +
												"1. Convert ALL IDs → names immediately\n" +
												"2. Show 1-line formula context (e.g., \"60% margin = (50-20)/50\")\n" +
												"3. Highlight negative margins as LOSSES\n" +
												"4. Compare prices: stockData.sellingPrice vs salesData.price\n" +
												"5. 3 sentences max, bold** key figures\n" +
												"6. Simplest and sortest that contains only important details no explanation unless asked for";
				break;
			case "customer":
				GeminiInitialString += "Act as Chief data Analyst + Data Scientest using this customer data:\n\n" + business_JO + business_JO + ".\n\n" +
												"Data Map :\n" +
												"1. prods: [ID: [Name, Category, Cost, TargetPrice, CurrentStock, GST%]]\n" +
												"2. custs: [ID: [Name, Phone]]\n" +
												"3. sales: [Date, BillNo, ProdID, CustID, Qty, SoldPrice, GST%]" +
												" Follow this decision tree:\n\n" +
												"1. For profits:\n" +
												"- Per Sale = (salesData.price × quantity) - (stockData.purchasePrice × quantity)\n" +
												"- Margin % = ((price - purchasePrice)/price)×100\n" +
												"- Total = Σ all salesData.subtotal - Σ (purchasePrice×quantity)\n" +
												"\n" +
												"2. GST Checks:\n" +
												"- For me my GST is TAX.\n" +
												"- Verify salesData.Gst matches productData.category's typical rate\n" +
												"- Flag deviations >±1% from category norms\n" +
												"\n" +
												"3. Inventory Analysis:\n" +
												"- Sales Velocity = total sold/(max_date - min_date in salesData)\n" +
												"- Stock Coverage = stockQuantityData.quantity / velocity\n" +
												"\n" +
												"4. Customer Value:\n" +
												"- Lifetime Spend = Σ subtotal by customerId\n" +
												"- Visit Frequency = salesData entries count per customerId\n" +
												"\n" +
												"Response MUST:\n" +
												"1. Convert ALL IDs → names immediately\n" +
												"2. Show 1-line formula context (e.g., \"60% margin = (50-20)/50\")\n" +
												"3. Highlight negative margins as LOSSES\n" +
												"4. Compare prices: stockData.sellingPrice vs salesData.price\n" +
												"5. 3 sentences max, bold** key figures\n" +
												"6. Simplest and sortest that contains only important details no explanation unless asked for";
				break;
		}

		recyclerView = findViewById(R.id.recyclerView_gemini);
		editTextMessage = findViewById(R.id.editTextMessage);
		Button buttonSend = findViewById(R.id.buttonSend);

		messageList = new ArrayList<>();
		messageAdapter = new MessageAdapter(messageList);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(messageAdapter);

		// Load messages from database ===================================================================
			new LoadMessagesTask(getApplicationContext(), sellerId, messageList, messageAdapter).execute();


		// The Gemini 1.5 models are versatile and work with multi-turn conversations (like chat)
		GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash", dbManager.getApiKey(email));
		GenerativeModelFutures model = GenerativeModelFutures.from(gm);
// Initialize the chat
		chat = model.startChat();

		// Send message button ===========================================================================
		buttonSend.setOnClickListener(v -> {
			messageText = editTextMessage.getText().toString();
			if (!messageText.isEmpty()) {
				// Add user message to the list and database
				ChatMessage userMessage = new ChatMessage(messageText, true, sellerId);
				saveMessage(userMessage);

				// Send the message to Gemini
				new SendMessageTask().execute(messageText);

				// Clear the input field
				editTextMessage.setText("");
			}
		});
// =================================================================================================

// Send the message to Gemini with data ============================================================
		if (!Query_type.equals("continue")) {

			// initial user message with data
			Content.Builder userContentBuilder3 = new Content.Builder();
			userContentBuilder3.setRole("user");
			userContentBuilder3.addText(GeminiInitialString);
			Content userContent3 = userContentBuilder3.build();

			ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userContent3);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
					@Override
					public void onSuccess(GenerateContentResponse result) {
						String resultText = result.getText();
						Toast.makeText(Gemini_chat.this, "Data successfully synced.", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(@NonNull Throwable t) {
						t.printStackTrace();
						Toast.makeText(Gemini_chat.this, "Failed to sync data. Please try again later", Toast.LENGTH_LONG).show();
					}
				}, getMainExecutor());
			}
		}
// =================================================================================================

		// Clear chat btn ================================================================================
		findViewById(R.id.fab_clear_chat).setOnClickListener(v -> Toast.makeText(Gemini_chat.this, "Long click to delete all chat", Toast.LENGTH_SHORT).show());

		findViewById(R.id.fab_clear_chat).setOnLongClickListener(v -> {
				if (dbManager.clearChat(String.valueOf(sellerId))) {
					messageList.clear();
					messageAdapter.notifyDataSetChanged();
					Toast.makeText(Gemini_chat.this, "Clear chat", Toast.LENGTH_SHORT).show();
				}
				return true;
		});
		// ===============================================================================================


	}

	// Save messages to database ======================================================================
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
// =================================================================================================

	// Load messages from database ====================================================================
	public static class LoadMessagesTask extends AsyncTask<Void, Void, List<ChatMessage>> {
		private DBManager dbManager;
		private final WeakReference<Context> contextRef;
		private final WeakReference<MessageAdapter> adapterRef;
		private final List<ChatMessage> messageList;
		private final int sellerId;

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

		@SuppressLint("NotifyDataSetChanged")
		@Override
		protected void onPostExecute(List<ChatMessage> chatMessages) {
			if (chatMessages != null && adapterRef.get() != null) {
				messageList.clear();
				messageList.addAll(chatMessages);
				adapterRef.get().notifyDataSetChanged();
				recyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the bottom
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
// =================================================================================================

	// Send message to Gemini =========================================================================
	@SuppressLint("StaticFieldLeak")
	private class SendMessageTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String userMessage = params[0];
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
						responseAI[0] = resultText;
						latch.countDown(); // Decrement the count of the latch, releasing all waiting threads
					}

					@Override
					public void onFailure(@NonNull Throwable t) {
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
				retryCount = 0;
			} else {
				if(retryCount < 3){
				Toast.makeText(Gemini_chat.this, "No respond on try:"+(retryCount+1)+"... Trying again...", Toast.LENGTH_SHORT).show();

				new SendMessageTask().execute(messageText);
				retryCount++;
				}else{
					Toast.makeText(Gemini_chat.this, "Somthing wrong with GEMINI try again in few hours", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
// =================================================================================================

}