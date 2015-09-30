package org.alfresco.plugin.digitalSigning.action.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

public class IsCertificateExpired extends BaseEvaluator{

	@Override
	public boolean evaluate(JSONObject arg0) {
		try {
			
			final String uri = "/api/digitalSigning/isCertificateExpired";

			final RequestContext context = ThreadLocalRequestContext.getRequestContext();
			if (context != null) {
				final Connector connector = context.getServiceRegistry().getConnectorService().getConnector("alfresco", context.getUserId(), ServletUtil.getSession());
				if (connector != null) {
					final Response res = connector.call(uri);
					if (res != null) {
						final String alfrescoWebScriptResponse = res.getResponse();
						if ("ok".compareTo(alfrescoWebScriptResponse) == 0) {
							return false;
						} else {
							return true;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}

}
