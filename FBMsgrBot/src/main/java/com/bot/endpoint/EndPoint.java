/*
 *
 */
package com.bot.endpoint;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.GraphResponse;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.Message;
import com.restfb.types.webhook.WebhookEntry;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.messaging.MessagingItem;

/**
 * Servlet implementation class EndPoint
 */
@WebServlet(name = "/webhook", urlPatterns = { "/webhook" })
public class EndPoint extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String accessToken = "FACEBOOK_ACCESS_TOKEN";

	/**
	 * Default constructor.
	 */
	public EndPoint() {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("inside doGet() method");

		if (request.getParameter("hub.verify_token").compareToIgnoreCase("testbot_verify_token") == 0) {
			response.getWriter().append(request.getParameter("hub.challenge"));
		} else {
			response.getWriter().append("Served at: ").append(request.getContextPath());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("inside doPost() method");
		// retrieve POST Body
		final String body = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

		// map body json to WebhookObject
		final DefaultJsonMapper mapper = new DefaultJsonMapper();
		final WebhookObject webhookObject = mapper.toJavaObject(body, WebhookObject.class);

		for (final WebhookEntry entry : webhookObject.getEntryList()) {
			if (!entry.getMessaging().isEmpty()) {
				for (final MessagingItem item : entry.getMessaging()) {
					final String senderId = item.getSender().getId();

					// create recipient
					final IdMessageRecipient recipient = new IdMessageRecipient(senderId);

					// check message
					if (item.getMessage() != null && item.getMessage().getText() != null) {
						// create simple text message
						final Message simpleTextMessage = new Message("Echo: " + item.getMessage().getText());

						// build send client and send message
						final FacebookClient sendClient = new DefaultFacebookClient(
								accessToken,
								Version.VERSION_2_6);
						sendClient.publish("me/messages", GraphResponse.class, Parameter.with("recipient", recipient),
								Parameter.with("message", simpleTextMessage));
					}

					if (item.getPostback() != null) {
						// LOG.debug("run postback");
					}
				}
			}
		}
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		System.out.println("inside init() method");
	}

}
