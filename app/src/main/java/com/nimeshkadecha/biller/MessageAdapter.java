package com.nimeshkadecha.biller;

import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final List<ChatMessage> messageList;

	private static final int VIEW_TYPE_USER = 1;
	private static final int VIEW_TYPE_AI = 2;

	public MessageAdapter(List<ChatMessage> messageList) {
		this.messageList = messageList;
	}

	@Override
	public int getItemViewType(int position) {
		ChatMessage message = messageList.get(position);
		if (message.isSentByUser()) {
			return VIEW_TYPE_USER;
		} else {
			return VIEW_TYPE_AI;
		}
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_USER) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
			return new UserMessageViewHolder(view);
		} else {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_ai, parent, false);
			return new AIMessageViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		ChatMessage message = messageList.get(position);
		if (holder instanceof UserMessageViewHolder) {
			((UserMessageViewHolder) holder).bind(message);
		} else if (holder instanceof AIMessageViewHolder) {
			((AIMessageViewHolder) holder).bind(message);
		}
	}

	@Override
	public int getItemCount() {
		return messageList.size();
	}

	static class UserMessageViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewMessage;

		public UserMessageViewHolder(@NonNull View itemView) {
			super(itemView);
			textViewMessage = itemView.findViewById(R.id.textViewMessage);
		}

		public void bind(ChatMessage message) {
			// Markdown text including tables
			String markdownText = message.getMessage();
			// Parse Markdown to HTML
			Parser parser = Parser.builder().build();
			HtmlRenderer renderer = HtmlRenderer.builder().build();
			String html = renderer.render(parser.parse(markdownText));

			textViewMessage.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
		}
	}

	static class AIMessageViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewMessage;

		public AIMessageViewHolder(@NonNull View itemView) {
			super(itemView);
			textViewMessage = itemView.findViewById(R.id.textViewMessage);
		}

		public void bind(ChatMessage message) {
			// Markdown text including tables
			String markdownText = message.getMessage();
			// Parse Markdown to HTML
			Parser parser = Parser.builder().build();
			HtmlRenderer renderer = HtmlRenderer.builder().build();
			String html = renderer.render(parser.parse(markdownText));

			textViewMessage.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
		}
	}
}
