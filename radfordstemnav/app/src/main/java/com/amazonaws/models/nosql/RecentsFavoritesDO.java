package com.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "radfordstemnav-mobilehub-1857620739-recents-favorites")

public class RecentsFavoritesDO {
    private String _userId;
    private String _name;
    private int _tTL;
    private String _category;
    private Double _latitude;
    private Double _longitude;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBIndexHashKey(attributeName = "userId", globalSecondaryIndexName = "user-category-index")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }

    @DynamoDBRangeKey(attributeName = "name")
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }

    @DynamoDBAttribute(attributeName = "TTL")
    public int getTTL() {
        return _tTL;
    }

    public void setTTL(final int _tTL) {
        this._tTL = _tTL;
    }

    @DynamoDBIndexHashKey(attributeName = "category", globalSecondaryIndexName = "Categories")
    @DynamoDBIndexRangeKey(attributeName = "category", globalSecondaryIndexName = "user-category-index")
    public String getCategory() {
        return _category;
    }

    public void setCategory(final String _category) {
        this._category = _category;
    }

    @DynamoDBAttribute(attributeName = "latitude")
    public Double getLatitude() {
        return _latitude;
    }

    public void setLatitude(final Double _latitude) {
        this._latitude = _latitude;
    }

    @DynamoDBIndexRangeKey(attributeName = "longitude", globalSecondaryIndexName = "Categories")
    public Double getLongitude() {
        return _longitude;
    }

    public void setLongitude(final Double _longitude) {
        this._longitude = _longitude;
    }

}
