package com.vls.dde.ret.api.client.dto.v1_0;

import com.vls.dde.ret.api.client.function.UnsafeSupplier;
import com.vls.dde.ret.api.client.serdes.v1_0.DdeReturnRequestSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class DdeReturnRequest implements Cloneable, Serializable {

	public static DdeReturnRequest toDTO(String json) {
		return DdeReturnRequestSerDes.toDTO(json);
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public void setBranch(
		UnsafeSupplier<String, Exception> branchUnsafeSupplier) {

		try {
			branch = branchUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String branch;

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public void setBranchCode(
		UnsafeSupplier<String, Exception> branchCodeUnsafeSupplier) {

		try {
			branchCode = branchCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String branchCode;

	public Integer getDocId() {
		return docId;
	}

	public void setDocId(Integer docId) {
		this.docId = docId;
	}

	public void setDocId(
		UnsafeSupplier<Integer, Exception> docIdUnsafeSupplier) {

		try {
			docId = docIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer docId;

	public Integer getMatchPercentage() {
		return matchPercentage;
	}

	public void setMatchPercentage(Integer matchPercentage) {
		this.matchPercentage = matchPercentage;
	}

	public void setMatchPercentage(
		UnsafeSupplier<Integer, Exception> matchPercentageUnsafeSupplier) {

		try {
			matchPercentage = matchPercentageUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Integer matchPercentage;

	public String getNbgCode() {
		return nbgCode;
	}

	public void setNbgCode(String nbgCode) {
		this.nbgCode = nbgCode;
	}

	public void setNbgCode(
		UnsafeSupplier<String, Exception> nbgCodeUnsafeSupplier) {

		try {
			nbgCode = nbgCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String nbgCode;

	public String getReltocntrct() {
		return reltocntrct;
	}

	public void setReltocntrct(String reltocntrct) {
		this.reltocntrct = reltocntrct;
	}

	public void setReltocntrct(
		UnsafeSupplier<String, Exception> reltocntrctUnsafeSupplier) {

		try {
			reltocntrct = reltocntrctUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String reltocntrct;

	public String getSignatoryName() {
		return signatoryName;
	}

	public void setSignatoryName(String signatoryName) {
		this.signatoryName = signatoryName;
	}

	public void setSignatoryName(
		UnsafeSupplier<String, Exception> signatoryNameUnsafeSupplier) {

		try {
			signatoryName = signatoryNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String signatoryName;

	public String getSignedDocData() {
		return signedDocData;
	}

	public void setSignedDocData(String signedDocData) {
		this.signedDocData = signedDocData;
	}

	public void setSignedDocData(
		UnsafeSupplier<String, Exception> signedDocDataUnsafeSupplier) {

		try {
			signedDocData = signedDocDataUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String signedDocData;

	public String getSignedName() {
		return signedName;
	}

	public void setSignedName(String signedName) {
		this.signedName = signedName;
	}

	public void setSignedName(
		UnsafeSupplier<String, Exception> signedNameUnsafeSupplier) {

		try {
			signedName = signedNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String signedName;

	public String getUin() {
		return uin;
	}

	public void setUin(String uin) {
		this.uin = uin;
	}

	public void setUin(UnsafeSupplier<String, Exception> uinUnsafeSupplier) {
		try {
			uin = uinUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String uin;

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public void setZone(UnsafeSupplier<String, Exception> zoneUnsafeSupplier) {
		try {
			zone = zoneUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String zone;

	@Override
	public DdeReturnRequest clone() throws CloneNotSupportedException {
		return (DdeReturnRequest)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DdeReturnRequest)) {
			return false;
		}

		DdeReturnRequest ddeReturnRequest = (DdeReturnRequest)object;

		return Objects.equals(toString(), ddeReturnRequest.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DdeReturnRequestSerDes.toJSON(this);
	}

}