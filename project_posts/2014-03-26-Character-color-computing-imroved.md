The program algorithm splits the original image into cells in size of the font character. Initially, the color of the character was determined by the color of the upper left pixel of the corresponding cell. Of course, such an approach does not give the most correct color. So I improved the algorithm and added computation of the average color for each cell.

Now generated images have a smoother color transitions from symbol to symbol, fewer artifacts expressed in the wrong color symbol.

Before:

![Initial algorithm result](../project_images/iteration2_sample1.jpg?raw=true "Initial algorithm result")

After:

![Enhanced algorithm result](../project_images/iteration4_sample1.jpg?raw=true "Enhanced algorithm result")
