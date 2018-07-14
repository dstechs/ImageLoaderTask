# ImageLoaderTask
Module for loading Image using memory cache and OkHttp3 also support Get Request

## Requirements

HttpRequestClient Library can be included in any Android application. 

HttpRequestClient Library supports Android 4.4 (Kitkat) and later. 

## Using HttpRequestClient Library in your application

```java
HttpRequestClient.initialize(getApplicationContext());
```
### Making a GET Request without params support available
```java
HttpRequestClient.get("http://pastebin.com/raw/wgkJgazE")
                 .setTag(DetailPresentor.class).build().getAsString(new StringRequestListener() {
                          @Override
                          public void onResponse(String response) {
                          // do something with response
                          }

                          @Override
                          public void onError(ILError anError) {
                          // handle error
                          }
                      });       
```
### Loading image from network into ImageView
```xml
       <com.generic.httpclient.widget.ILImageView
            android:id="@+id/ivImage"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
          
      imageView.setDefaultImageResId(R.drawable.default);
      imageView.setErrorImageResId(R.drawable.error);
      imageView.setImageUrl(imageUrl);          
```
### Getting Bitmap from url
```java
HttpRequestClient.get("http://pastebin.com/raw/wgkJgazE")
                 .setTag("imageRequestTag")
                 .setPriority(Priority.MEDIUM)
                 .setBitmapMaxHeight(100)
                 .setBitmapMaxWidth(100)
                 .setBitmapConfig(Bitmap.Config.ARGB_8888)
                 .build()
                 .getAsBitmap(new BitmapRequestListener() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                    // do anything with bitmap
                    }
                    @Override
                    public void onError(ANError error) {
                      // handle error
                    }
                });
```
### Use can also use ImageLoader
```java
 ImageLoader.getInstance().get(url, getImageListener(ivView, R.drawable.ic_place_holder, R.drawable.ic_image_error));
```

### Remove Bitmap from cache
```java
HttpRequestClient.evictBitmap(key);
HttpRequestClient.evictAllBitmap();
```
