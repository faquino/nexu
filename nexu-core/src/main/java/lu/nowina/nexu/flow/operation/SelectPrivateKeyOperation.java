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
package lu.nowina.nexu.flow.operation;

import java.util.Iterator;
import java.util.List;

import lu.nowina.nexu.api.CardAdapter;
import lu.nowina.nexu.api.CertificateFilter;
import lu.nowina.nexu.api.DetectedCard;
import lu.nowina.nexu.view.core.UIOperation;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

/**
 * This {@link CompositeOperation} allows to retrieve a private key for a given {@link SignatureTokenConnection}
 * and optional <code>certificate filter</code> and/or <code>key filter</code>.
 *
 * Expected parameters:
 * <ol>
 * <li>{@link SignatureTokenConnection}</li>
 * <li>{@link DetectedCard} (optional)</li>
 * <li>{@link CardAdapter} (optional)</li>
 * <li>{@link CertificateFilter} (optional)</li>
 * <li>Key filter (optional): {@link String}</li>
 * </ol>
 *
 * @author Jean Lepropre (jean.lepropre@nowina.lu)
 */
public class SelectPrivateKeyOperation extends AbstractCompositeOperation<DSSPrivateKeyEntry> {

	private SignatureTokenConnection token;
	private DetectedCard card;
	private CardAdapter cardAdapter;
	private CertificateFilter certificateFilter;
	private String keyFilter;
	
	public SelectPrivateKeyOperation() {
		super();
	}

	@Override
	public void setParams(Object... params) {
		try {
			this.token = (SignatureTokenConnection) params[0];
			if(params.length > 1) {
				this.card = (DetectedCard) params[1];
			}
			if(params.length > 2) {
				this.cardAdapter = (CardAdapter) params[2];
			}
			if(params.length > 3) {
				certificateFilter = (CertificateFilter) params[3];
			}
			if(params.length > 4) {
				keyFilter = (String) params[4];
			}
		} catch(final ClassCastException | ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Expected parameters: SignatureTokenConnection, DetectedCard (optional), CardAdapter (optional), CertificateFilter (optional), key filter (optional)");
		}
	}
	
	@Override
	public OperationResult<DSSPrivateKeyEntry> perform() {
		final List<DSSPrivateKeyEntry> keys;
		if((cardAdapter != null) && (card != null) && cardAdapter.supportCertificateFilter(card) && (certificateFilter != null)) {
			keys = cardAdapter.getKeys(token, certificateFilter);
		} else {
			keys = token.getKeys();
		}
		
		DSSPrivateKeyEntry key = null;

		final Iterator<DSSPrivateKeyEntry> it = keys.iterator();
		while (it.hasNext()) {
			final DSSPrivateKeyEntry e = it.next();
			if ("CN=Token Signing Public Key".equals(e.getCertificate().getIssuerDN().getName())) {
				it.remove();
			}
		}

		if (keys.isEmpty()) {
			return new OperationResult<DSSPrivateKeyEntry>(OperationStatus.FAILED);
		} else if (keys.size() == 1) {
			key = keys.get(0);
			if((keyFilter != null) && !key.getCertificate().getDSSIdAsString().equals(keyFilter)) {
				return new OperationResult<DSSPrivateKeyEntry>(OperationStatus.FAILED);
			} else {
				return new OperationResult<DSSPrivateKeyEntry>(key);
			}
		} else {
			if (keyFilter != null) {
				for (final DSSPrivateKeyEntry k : keys) {
					if (k.getCertificate().getDSSIdAsString().equals(keyFilter)) {
						key = k;
						break;
					}
				}
				if(key == null) {
					return new OperationResult<DSSPrivateKeyEntry>(OperationStatus.FAILED);
				}
			} else {
				@SuppressWarnings("unchecked")
				final OperationResult<DSSPrivateKeyEntry> op =
						operationFactory.getOperation(UIOperation.class, display, "/fxml/key-selection.fxml", new Object[]{keys}).perform();
				if(!op.getStatus().equals(OperationStatus.SUCCESS)) {
					return new OperationResult<DSSPrivateKeyEntry>(OperationStatus.FAILED);
				}
				key = op.getResult();
			}
			return new OperationResult<DSSPrivateKeyEntry>(key);
		}
	}
}