package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.dao.MessageDao;
import bsu.fpmi.chat.dao.MessageDaoImpl;
import bsu.fpmi.chat.util.ServletUtil;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static bsu.fpmi.chat.util.ServletUtil.TOKEN;
import static bsu.fpmi.chat.util.ServletUtil.getIndex;

public final class AsyncProcessor {
	private final static Queue<AsyncContext> storage = new ConcurrentLinkedQueue<AsyncContext>();
	private static Logger logger;
	private static MessageDao messageDao = new MessageDaoImpl();

	public static void notifyAllClients() {
		for (AsyncContext asyncContext : storage) {
			getMessages(asyncContext);
			asyncContext.complete();
			storage.remove(asyncContext);
		}
	}

	public static void getMessages(AsyncContext asyncContext) {
		String token = asyncContext.getRequest().getParameter(TOKEN);

		if (token != null && !"".equals(token)) {
			int index = getIndex(token);

			try {
				String messages;
				if (index == 0) {
					messages = messageDao.getAll().toJSONString();
				}
				else {
					//messages = XMLHistoryParser.getMessagesFrom(index);
					messages = messageDao.getMessagesFromRequest(index).toJSONString();
				}

				asyncContext.getResponse().setContentType(ServletUtil.APPLICATION_JSON);
				asyncContext.getResponse().setCharacterEncoding("utf-8");

				PrintWriter out = asyncContext.getResponse().getWriter();
				out.print(messages);
				out.flush();
			} catch (SAXException | IOException | ParserConfigurationException e) {
				logger.error(e);
			}
		}

		/* else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
		}*/
	}

	public static void addAsyncContext(final AsyncContext context, Logger getLogger) {
		context.addListener(new AsyncListener() {

			public void onTimeout(AsyncEvent event) throws IOException {
				removeAsyncContext(context);
			}

			public void onStartAsync(AsyncEvent event) throws IOException {
			}

			public void onError(AsyncEvent event) throws IOException {
				removeAsyncContext(context);
			}

			public void onComplete(AsyncEvent event) throws IOException {
				removeAsyncContext(context);
			}
		});

		storage.add(context);
		logger = getLogger;
	}

	private static void removeAsyncContext(final AsyncContext context) {
		storage.remove(context);
	}

}
