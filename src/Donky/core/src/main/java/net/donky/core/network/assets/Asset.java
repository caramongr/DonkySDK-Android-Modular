package net.donky.core.network.assets;

import com.google.gson.annotations.SerializedName;

/**
 * The meta-data for an asset.
 */
public class Asset {

	@SerializedName("assetId")
	private String assetId;

	@SerializedName("mimeType")
	private String mimeType;

	@SerializedName("name")
	private String name;

	@SerializedName("sizeInBytes")
	private Long sizeInBytes;

    private String filepath;

	public Asset(String name, String mimeType, long sizeInBytes) {
		this.name = name;
		this.mimeType = mimeType;
		this.sizeInBytes = sizeInBytes;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getSizeInBytes() {
		return sizeInBytes;
	}

	public void setSizeInBytes(Long sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}

    public String getFilePath() {
        return filepath;
    }

    public void setFilePath(String filePath) {
        this.filepath = filePath;
    }
}