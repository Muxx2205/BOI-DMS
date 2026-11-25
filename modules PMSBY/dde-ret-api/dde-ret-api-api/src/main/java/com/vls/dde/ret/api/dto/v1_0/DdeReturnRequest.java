package com.vls.dde.ret.api.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Generated;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shehn
 * @generated
 */
@Generated("")
@GraphQLName("DdeReturnRequest")
@JsonFilter("Liferay.Vulcan")
@Schema(
	requiredProperties = {
		"docId", "uin", "reltocntrct", "signedDocData", "signatoryName",
		"signedName", "matchPercentage", "branchCode"
	}
)
@XmlRootElement(name = "DdeReturnRequest")
public class DdeReturnRequest implements Serializable {

	public static DdeReturnRequest toDTO(String json) {
		return ObjectMapperUtil.readValue(DdeReturnRequest.class, json);
	}

	public static DdeReturnRequest unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(DdeReturnRequest.class, json);
	}

	@Schema(
		description = "Branch name (optional - auto-resolved from Organization if not provided)."
	)
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@JsonIgnore
	public void setBranch(
		UnsafeSupplier<String, Exception> branchUnsafeSupplier) {

		try {
			branch = branchUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Branch name (optional - auto-resolved from Organization if not provided)."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String branch;

	@Schema(
		description = "Branch code - first 4 digits are extracted to resolve branch hierarchy."
	)
	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	@JsonIgnore
	public void setBranchCode(
		UnsafeSupplier<String, Exception> branchCodeUnsafeSupplier) {

		try {
			branchCode = branchCodeUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Branch code - first 4 digits are extracted to resolve branch hierarchy."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String branchCode;

	@Schema(
		description = "Document reference ID passed in the DDE API request."
	)
	public Integer getDocId() {
		return docId;
	}

	public void setDocId(Integer docId) {
		this.docId = docId;
	}

	@JsonIgnore
	public void setDocId(
		UnsafeSupplier<Integer, Exception> docIdUnsafeSupplier) {

		try {
			docId = docIdUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Document reference ID passed in the DDE API request."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotNull
	protected Integer docId;

	@DecimalMax("100")
	@DecimalMin("0")
	@Schema(
		description = "Percentage matched between the signer and signed name."
	)
	public Integer getMatchPercentage() {
		return matchPercentage;
	}

	public void setMatchPercentage(Integer matchPercentage) {
		this.matchPercentage = matchPercentage;
	}

	@JsonIgnore
	public void setMatchPercentage(
		UnsafeSupplier<Integer, Exception> matchPercentageUnsafeSupplier) {

		try {
			matchPercentage = matchPercentageUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Percentage matched between the signer and signed name."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotNull
	protected Integer matchPercentage;

	@Schema(
		description = "NBG code (optional - auto-resolved from Organization if not provided)."
	)
	public String getNbgCode() {
		return nbgCode;
	}

	public void setNbgCode(String nbgCode) {
		this.nbgCode = nbgCode;
	}

	@JsonIgnore
	public void setNbgCode(
		UnsafeSupplier<String, Exception> nbgCodeUnsafeSupplier) {

		try {
			nbgCode = nbgCodeUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "NBG code (optional - auto-resolved from Organization if not provided)."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String nbgCode;

	@Schema(
		description = "Relation to the contract (e.g., Debtor, Co-obligant)."
	)
	public String getReltocntrct() {
		return reltocntrct;
	}

	public void setReltocntrct(String reltocntrct) {
		this.reltocntrct = reltocntrct;
	}

	@JsonIgnore
	public void setReltocntrct(
		UnsafeSupplier<String, Exception> reltocntrctUnsafeSupplier) {

		try {
			reltocntrct = reltocntrctUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Relation to the contract (e.g., Debtor, Co-obligant)."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String reltocntrct;

	@Schema(description = "Signer name that was passed in the initial JSON.")
	public String getSignatoryName() {
		return signatoryName;
	}

	public void setSignatoryName(String signatoryName) {
		this.signatoryName = signatoryName;
	}

	@JsonIgnore
	public void setSignatoryName(
		UnsafeSupplier<String, Exception> signatoryNameUnsafeSupplier) {

		try {
			signatoryName = signatoryNameUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Signer name that was passed in the initial JSON."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String signatoryName;

	@Schema(description = "Base64 value of the signed document.")
	public String getSignedDocData() {
		return signedDocData;
	}

	public void setSignedDocData(String signedDocData) {
		this.signedDocData = signedDocData;
	}

	@JsonIgnore
	public void setSignedDocData(
		UnsafeSupplier<String, Exception> signedDocDataUnsafeSupplier) {

		try {
			signedDocData = signedDocDataUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(description = "Base64 value of the signed document.")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String signedDocData;

	@Schema(description = "Aadhaar name of the person who signed the document.")
	public String getSignedName() {
		return signedName;
	}

	public void setSignedName(String signedName) {
		this.signedName = signedName;
	}

	@JsonIgnore
	public void setSignedName(
		UnsafeSupplier<String, Exception> signedNameUnsafeSupplier) {

		try {
			signedName = signedNameUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Aadhaar name of the person who signed the document."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String signedName;

	@Schema(
		description = "Unique Identification Number passed in the DDE API request."
	)
	public String getUin() {
		return uin;
	}

	public void setUin(String uin) {
		this.uin = uin;
	}

	@JsonIgnore
	public void setUin(UnsafeSupplier<String, Exception> uinUnsafeSupplier) {
		try {
			uin = uinUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Unique Identification Number passed in the DDE API request."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	@NotEmpty
	protected String uin;

	@Schema(
		description = "Zone name (optional - auto-resolved from Organization if not provided)."
	)
	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	@JsonIgnore
	public void setZone(UnsafeSupplier<String, Exception> zoneUnsafeSupplier) {
		try {
			zone = zoneUnsafeSupplier.get();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GraphQLField(
		description = "Zone name (optional - auto-resolved from Organization if not provided)."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String zone;

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
		StringBundler sb = new StringBundler();

		sb.append("{");

		if (branch != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branch\": ");

			sb.append("\"");

			sb.append(_escape(branch));

			sb.append("\"");
		}

		if (branchCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"branchCode\": ");

			sb.append("\"");

			sb.append(_escape(branchCode));

			sb.append("\"");
		}

		if (docId != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"docId\": ");

			sb.append(docId);
		}

		if (matchPercentage != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"matchPercentage\": ");

			sb.append(matchPercentage);
		}

		if (nbgCode != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"nbgCode\": ");

			sb.append("\"");

			sb.append(_escape(nbgCode));

			sb.append("\"");
		}

		if (reltocntrct != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"reltocntrct\": ");

			sb.append("\"");

			sb.append(_escape(reltocntrct));

			sb.append("\"");
		}

		if (signatoryName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"signatoryName\": ");

			sb.append("\"");

			sb.append(_escape(signatoryName));

			sb.append("\"");
		}

		if (signedDocData != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"signedDocData\": ");

			sb.append("\"");

			sb.append(_escape(signedDocData));

			sb.append("\"");
		}

		if (signedName != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"signedName\": ");

			sb.append("\"");

			sb.append(_escape(signedName));

			sb.append("\"");
		}

		if (uin != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"uin\": ");

			sb.append("\"");

			sb.append(_escape(uin));

			sb.append("\"");
		}

		if (zone != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"zone\": ");

			sb.append("\"");

			sb.append(_escape(zone));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.vls.dde.ret.api.dto.v1_0.DdeReturnRequest",
		name = "x-class-name"
	)
	public String xClassName;

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>)value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

}