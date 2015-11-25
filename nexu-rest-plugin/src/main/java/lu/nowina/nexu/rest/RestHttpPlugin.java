/**
 * © Nowina Solutions, 2015-2015
 *
 * Concédée sous licence EUPL, version 1.1 ou – dès leur approbation par la Commission européenne - versions ultérieures de l’EUPL (la «Licence»).
 * Vous ne pouvez utiliser la présente œuvre que conformément à la Licence.
 * Vous pouvez obtenir une copie de la Licence à l’adresse suivante:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Sauf obligation légale ou contractuelle écrite, le logiciel distribué sous la Licence est distribué «en l’état»,
 * SANS GARANTIES OU CONDITIONS QUELLES QU’ELLES SOIENT, expresses ou implicites.
 * Consultez la Licence pour les autorisations et les restrictions linguistiques spécifiques relevant de la Licence.
 */
package lu.nowina.nexu.rest;

import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.ToBeSigned;
import lu.nowina.nexu.api.Execution;
import lu.nowina.nexu.api.GetCertificateRequest;
import lu.nowina.nexu.api.GetIdentityInfoRequest;
import lu.nowina.nexu.api.NexuAPI;
import lu.nowina.nexu.api.SignatureRequest;
import lu.nowina.nexu.api.TokenId;
import lu.nowina.nexu.api.plugin.HttpPlugin;
import lu.nowina.nexu.api.plugin.HttpRequest;
import lu.nowina.nexu.api.plugin.HttpResponse;
import lu.nowina.nexu.api.plugin.HttpStatus;

/**
 * Default implementation of HttpPlugin for NexU.
 *
 * @author David Naramski
 */
public class RestHttpPlugin implements HttpPlugin {

	private static final Logger logger = Logger.getLogger(RestHttpPlugin.class.getName());

	private static final Gson gson = GsonHelper.customGson;

	@Override
	public void init(String pluginId, NexuAPI api) {
	}

	@Override
	public HttpResponse process(NexuAPI api, HttpRequest req) throws Exception {

		final String target = req.getTarget();
		logger.info("PathInfo " + target);

		final String payload = IOUtils.toString(req.getInputStream());
		logger.info("Payload '" + payload + "'");

		switch(target) {
		case "/sign":
			return signRequest(api, req, payload);
		case "/certificates":
			return getCertificates(api, payload);
		case "/identityInfo":
			return getIdentityInfo(api, payload);
		default:
			throw new RuntimeException("Target not recognized " + target);
		}
	}

	private HttpResponse signRequest(NexuAPI api, HttpRequest req, String payload) {
		logger.info("Signature");
		final SignatureRequest r;
		if (StringUtils.isEmpty(payload)) {
			r = new SignatureRequest();

			String data = req.getParameter("dataToSign");
			if (data != null) {
				logger.info("Data to sign " + data);
				ToBeSigned tbs = new ToBeSigned();
				tbs.setBytes(DatatypeConverter.parseBase64Binary(data));
				r.setToBeSigned(tbs);
			}

			String digestAlgo = req.getParameter("digestAlgo");
			if (digestAlgo != null) {
				logger.info("digestAlgo " + digestAlgo);
				r.setDigestAlgorithm(DigestAlgorithm.forName(digestAlgo, DigestAlgorithm.SHA256));
			}

			String tokenIdString = req.getParameter("tokenId");
			if (tokenIdString != null) {
				TokenId tokenId = new TokenId(tokenIdString);
				r.setTokenId(tokenId);
			}

			String keyId = req.getParameter("keyId");
			if (keyId != null) {
				r.setKeyId(keyId);
			}
		} else {
			r = gson.fromJson(payload, SignatureRequest.class);
		}

		final Execution<?> respObj = api.sign(r);
		return toHttpResponse(respObj);
	}

	private HttpResponse getCertificates(NexuAPI api, String payload) {
		logger.info("API call certificates");
		final GetCertificateRequest payloadObj;
		if (StringUtils.isEmpty(payload)) {
			payloadObj = new GetCertificateRequest();
		} else {
			payloadObj = gson.fromJson(payload, GetCertificateRequest.class);
		}

		logger.info("Call API");
		final Execution<?> respObj = api.getCertificate(payloadObj);
		return toHttpResponse(respObj);
	}

	private HttpResponse getIdentityInfo(NexuAPI api, String payload) {
		logger.info("API call get identity info");
		final GetIdentityInfoRequest payloadObj;
		if (StringUtils.isEmpty(payload)) {
			payloadObj = new GetIdentityInfoRequest();
		} else {
			payloadObj = gson.fromJson(payload, GetIdentityInfoRequest.class);
		}

		logger.info("Call API");
		final Execution<?> respObj = api.getIdentityInfo(payloadObj);
		return toHttpResponse(respObj);
	}
	
	private HttpResponse toHttpResponse(final Execution<?> respObj) {
		if (respObj.isSuccess()) {
			return new HttpResponse(gson.toJson(respObj), "application/json", HttpStatus.OK);
		} else {
			return new HttpResponse(gson.toJson(respObj), "application/json", HttpStatus.ERROR);
		}
	}
}
