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
package lu.nowina.nexu.api;

import java.util.List;

import eu.europa.esig.dss.token.SignatureTokenConnection;
import lu.nowina.nexu.api.signature.smartcard.CardAdapter;
import lu.nowina.nexu.api.signature.smartcard.TokenId;

/**
 * The API exposes the functionalities to the developer of Plugin. 
 * 
 * @author David Naramski
 */
public interface NexuAPI {

	List<DetectedCard> detectCards();

	List<Match> matchingCardAdapters(DetectedCard d);

	EnvironmentInfo getEnvironmentInfo();

	void registerCardAdapter(CardAdapter adapter);

	TokenId registerTokenConnection(SignatureTokenConnection connection);
	
	SignatureTokenConnection getTokenConnection(TokenId tokenId);
	
	Execution<GetCertificateResponse> getCertificate(GetCertificateRequest request);
	
	Execution<SignatureResponse> sign(SignatureRequest request);
	
}