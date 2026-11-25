package com.boi.headless.rest.jansuraksha.client.dto.v1_0;

import com.boi.headless.rest.jansuraksha.client.function.UnsafeSupplier;
import com.boi.headless.rest.jansuraksha.client.serdes.v1_0.JanSurakshaMetadataSerDes;

import java.io.Serializable;

import java.util.Date;
import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public class JanSurakshaMetadata implements Cloneable, Serializable {

	public static JanSurakshaMetadata toDTO(String json) {
		return JanSurakshaMetadataSerDes.toDTO(json);
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void setAccountNumber(
		UnsafeSupplier<String, Exception> accountNumberUnsafeSupplier) {

		try {
			accountNumber = accountNumberUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String accountNumber;

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

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public void setCustomerId(
		UnsafeSupplier<String, Exception> customerIdUnsafeSupplier) {

		try {
			customerId = customerIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String customerId;

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public void setCustomerName(
		UnsafeSupplier<String, Exception> customerNameUnsafeSupplier) {

		try {
			customerName = customerNameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String customerName;

	public String getNBGCode() {
		return nBGCode;
	}

	public void setNBGCode(String nBGCode) {
		this.nBGCode = nBGCode;
	}

	public void setNBGCode(
		UnsafeSupplier<String, Exception> nBGCodeUnsafeSupplier) {

		try {
			nBGCode = nBGCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String nBGCode;

	public String getSolId() {
		return solId;
	}

	public void setSolId(String solId) {
		this.solId = solId;
	}

	public void setSolId(
		UnsafeSupplier<String, Exception> solIdUnsafeSupplier) {

		try {
			solId = solIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String solId;

	public Date getTransDate() {
		return transDate;
	}

	public void setTransDate(Date transDate) {
		this.transDate = transDate;
	}

	public void setTransDate(
		UnsafeSupplier<Date, Exception> transDateUnsafeSupplier) {

		try {
			transDate = transDateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Date transDate;

	public TypeOfInsurance getTypeOfInsurance() {
		return typeOfInsurance;
	}

	public String getTypeOfInsuranceAsString() {
		if (typeOfInsurance == null) {
			return null;
		}

		return typeOfInsurance.toString();
	}

	public void setTypeOfInsurance(TypeOfInsurance typeOfInsurance) {
		this.typeOfInsurance = typeOfInsurance;
	}

	public void setTypeOfInsurance(
		UnsafeSupplier<TypeOfInsurance, Exception>
			typeOfInsuranceUnsafeSupplier) {

		try {
			typeOfInsurance = typeOfInsuranceUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected TypeOfInsurance typeOfInsurance;

	public String getUrn() {
		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}

	public void setUrn(UnsafeSupplier<String, Exception> urnUnsafeSupplier) {
		try {
			urn = urnUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String urn;

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setYear(UnsafeSupplier<String, Exception> yearUnsafeSupplier) {
		try {
			year = yearUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String year;

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
	public JanSurakshaMetadata clone() throws CloneNotSupportedException {
		return (JanSurakshaMetadata)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof JanSurakshaMetadata)) {
			return false;
		}

		JanSurakshaMetadata janSurakshaMetadata = (JanSurakshaMetadata)object;

		return Objects.equals(toString(), janSurakshaMetadata.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return JanSurakshaMetadataSerDes.toJSON(this);
	}

	public static enum TypeOfInsurance {

		PMJJBY("PMJJBY"), PMSBY("PMSBY");

		public static TypeOfInsurance create(String value) {
			for (TypeOfInsurance typeOfInsurance : values()) {
				if (Objects.equals(typeOfInsurance.getValue(), value) ||
					Objects.equals(typeOfInsurance.name(), value)) {

					return typeOfInsurance;
				}
			}

			return null;
		}

		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private TypeOfInsurance(String value) {
			_value = value;
		}

		private final String _value;

	}

}