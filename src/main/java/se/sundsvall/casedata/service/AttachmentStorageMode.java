package se.sundsvall.casedata.service;

/**
 * Controls how the binary content of a newly created attachment is persisted while migrating from base64 text storage
 * to binary blob storage. The read path is always backward compatible (prefers the blob, falls back to decoding the
 * legacy base64 {@code file} column), independently of this mode.
 */
public enum AttachmentStorageMode {

	/**
	 * Legacy behaviour: write only the base64 {@code file} column. Rollback target.
	 */
	BASE64,

	/**
	 * Transition behaviour: write both the base64 {@code file} column and the binary {@code content} blob + {@code hash}.
	 * Lets both the old and the new code read every row, so a full version rollback is safe (at the cost of ~2x storage
	 * for rows created during the transition).
	 */
	DUAL,

	/**
	 * End state: write only the binary {@code content} blob + {@code hash}.
	 */
	BLOB;

	/**
	 * @return {@code true} if this mode writes the legacy base64 {@code file} column.
	 */
	public boolean writesBase64() {
		return this == BASE64 || this == DUAL;
	}

	/**
	 * @return {@code true} if this mode writes the binary {@code content} blob and its {@code hash}.
	 */
	public boolean writesBlob() {
		return this == DUAL || this == BLOB;
	}
}
