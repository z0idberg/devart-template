# ASCII Art Dreaming

## Authors

- Aleksandr Knyazhev, https://github.com/z0idberg

## Description

This application is a screensaver for Android that displays photos from your device styled as ASCII artwork. I thought that the user may be interested to look at his photos in a new way. I developed an screensaver application (also known as daydream) that appeared in Android 4.2 and run while the machine is idle and/or connected to dock station or charger.

## Link to Prototype

![App on Google Play](https://play.google.com/store/search?q=ru.softinvent.ascii)

## Example Code

Split source image to rows and columns - row's height equals height of the font symbols and column's width equals width of the font symbols. Save coordinates of the left top pixel of each cell and one's average color.

```
private ArrayList<int[]> getAvgPixelsList(Bitmap bmp, int stepX, int stepY) {
	int xStepsCount = bmp.getWidth() / stepX;
	int yStepsCount = bmp.getHeight() / stepY;
	ArrayList<int[]> res = new ArrayList<>(xStepsCount * yStepsCount);
	for (int y = 0; y < yStepsCount; y++) {
		for (int x = 0; x < xStepsCount; x++) {
			res.add(new int[] {x * stepX, y * stepY, getAvgColor(bmp, x, y, stepX, stepY)});
		}
	}
	return res;
}
```

Next, select random character and put it to the corresponding cell with average color of this cell.

```
ArrayList<int[]> res = getAvgPixelsList(bmp, size.x, size.y);
int charsCount = res.size();
for (int i = 0; i < charsCount; i++) {
	txtPaint.setColor(res.get(i)[2]);
	canvas.drawText(getNextChar(), res.get(i)[0], res.get(i)[1], txtPaint);
}
```

## Links to External Libraries

No external libraries used, just pure Android.

## Images & Videos

Sample image produced by screensaver:

![Example Image](project_images/cover.jpg?raw=true "Example Image")

How it looks like on the phone (HD quality recommended):

http://youtu.be/0wJQkmD47y0