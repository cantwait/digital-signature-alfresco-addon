package org.alfresco.plugin.digitalSigning.webscript;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.plugin.digitalSigning.model.SigningConstants;
import org.alfresco.plugin.digitalSigning.model.SigningModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.ibm.icu.util.Calendar;

/**
 * 
 * Verify if the certificate has expired, so the option won't be shown
 * 
 * @category Evaluator webscript
 * 
 * @version 1.0
 * 
 * @author rafael
 *
 */
public class IsCertificateExpired extends AbstractWebScript {
	
	private static final String OK = "ok";
	private static final String KO = "ko";

	/**
	 * Logger.
	 */
	private final Log log = LogFactory.getLog(IsCertificateExpired.class);

	/**
	 * RetryingTransactionHelper bean.
	 */
	private RetryingTransactionHelper retryingTransactionHelper;

	/**
	 * Node service.
	 */
	private NodeService nodeService;

	/**
	 * Person service.
	 */
	private PersonService personService;

	/**
	 * Authentication service.
	 */
	private AuthenticationService authenticationService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
	
		// we get the user currently authenticated into alfresco share
		final String currentUser = authenticationService.getCurrentUserName();
		
		final RetryingTransactionCallback<Object> processCallBack = new RetryingTransactionCallback<Object>() {

			@Override
			public Object execute() throws Throwable {
				String result = KO;
				final NodeRef currentUserNodeRef = personService.getPerson(currentUser);
				if (currentUserNodeRef != null) {
					NodeRef keyNodeRef = null;

					final NodeRef currentUserHomeFolder = (NodeRef) nodeService.getProperty(currentUserNodeRef,	ContentModel.PROP_HOMEFOLDER);
					if (currentUserHomeFolder != null) {
						final NodeRef signingFolderNodeRef = nodeService.getChildByName(currentUserHomeFolder,ContentModel.ASSOC_CONTAINS,SigningConstants.KEY_FOLDER);
						if (signingFolderNodeRef != null) {
							final List<ChildAssociationRef> children = nodeService.getChildAssocs(signingFolderNodeRef);
							if (children != null && children.size() > 0) {
								
								final Iterator<ChildAssociationRef> itChildren = children.iterator();
								boolean foundKey = false;
								while (itChildren.hasNext()) {
									final ChildAssociationRef childAssoc = itChildren.next();
									final NodeRef child = childAssoc.getChildRef();
									if (nodeService.hasAspect(child,SigningModel.ASPECT_KEY)) {
										keyNodeRef = child;
										foundKey = true;
									}
								}
								if(!foundKey){
									result = OK;
								}else{
									Date currentDate = Calendar.getInstance().getTime();
									Date dateOfExpiricy = (Date) nodeService.getProperty(keyNodeRef, SigningModel.PROP_KEYLASTVALIDITY);
									if(log.isDebugEnabled()){
										log.debug(String.format("Date of expiricy: %tc and Current date: %tc", dateOfExpiricy, currentDate));
									}
									boolean dateOfExpiricyIsBeforeCurrentDate = currentDate.after(dateOfExpiricy);
									result = dateOfExpiricyIsBeforeCurrentDate ? OK : result;
									
								}
							}else{
								result = OK;
							}
						}
					}

				}
				res.getWriter().write(result);
				return result;
			};
		};
		
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {

			public Object doWork() throws Exception {
				retryingTransactionHelper.doInTransaction(processCallBack,false, false);
				return null;
			}
		}, currentUser);
	}

	public void setRetryingTransactionHelper(
			RetryingTransactionHelper retryingTransactionHelper) {
		this.retryingTransactionHelper = retryingTransactionHelper;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	
}
