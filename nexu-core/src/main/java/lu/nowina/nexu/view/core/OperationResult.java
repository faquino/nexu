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
package lu.nowina.nexu.view.core;

public class OperationResult<R> {

	private R result;
	
	private OperationStatus status;
	
	private Exception exception;

	public OperationResult(R result) {
		this.status = OperationStatus.SUCCESS;
		this.result = result;
	}
	
	public OperationResult(Exception ex) {
		this.status = OperationStatus.EXCEPTION;
		this.exception = ex;
	}

	public OperationResult(OperationStatus status) {
		this.status = status;
	}

	public Exception getException() {
		return exception;
	}
	
	public R getResult() {
		return result;
	}
	
	public OperationStatus getStatus() {
		return status;
	}
	
}
