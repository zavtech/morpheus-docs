## Principal Component Analysis

[Principal Component Analysis](https://en.wikipedia.org/wiki/Principal_component_analysis) (PCA) is a statistical tool 
used in data analysis and also for building predictive models. The technique involves transforming a dataset into a new 
[basis](https://en.wikipedia.org/wiki/Basis_(linear_algebra)) whereby the transformed data is uncorrelated. The transformed 
basis, which can be represented by an [orthogonal matrix](https://en.wikipedia.org/wiki/Orthogonal_matrix), defines the 
Principal Components of the original dataset. These basis vectors are usually ordered so that the first principal component 
is the one that accounts for the largest variance in the data, and the last component accounts for the least variance.

PCA is also often referred to as a [dimensional reduction](https://en.wikipedia.org/wiki/Dimensionality_reduction) technique
in the sense that by dropping components that account for a negligible amount of variance in the original data, we can linearly
map the data into a lower dimensional space without loosing material information. The assumption here is that the variability
in the data represents the essential dynamics we are trying to understand, so dropping dimensions with negligible variance
results in minimal loss of information.

The following sections introduces some PCA theory, and then proceeds to illustrate an example of how to use the Morpheus API
to perform PCA on a dataset. Only a superficial overview of the theory is covered below, so for a more detailed treatment of 
the topic I would suggest a Google search, or perhaps [this](https://www.cs.princeton.edu/picasso/mats/PCA-Tutorial-Intuition_jp.pdf)
tutorial as a primer.

## Theory

As a data analysis technique, PCA begins with the definition of our data which in general can be described in two dimensions,
namely the number of observations and the number of measurements. Such data can be represented by an `nxp` matrix where `n` 
represents the number of observations for each measurement, and `p` represents the number of measurements being recorded. 

The dimensions in which we record the observations that constitute our data are assumed to be a naive basis, since we do not 
actually understand the true dynamics of the system we are investigating (hence the analysis). Having said that, our hope is 
that while our measurements may be recorded in a naive basis, they are informative enough so that we can compute a new basis 
that maximises the signal to noise ratio and removes any redundancy in the data, enabling us to better understand its true 
dynamics. 

With this in mind, let us assume that there exists an [orthogonal matrix](https://en.wikipedia.org/wiki/Orthogonal_matrix) 
V of dimensions `pxp` that can transform our data X into Y such that the covariance matrix of Y (denoted by \\(\Sigma_{y}\\)) 
is diagonal.

$$ X V = Y $$

The question then is how to find V? We can tackle this by working backwards based on our desire that the transformed data Y 
has a diagonal covariance matrix, given our motivation to remove noise and redundancy from our dataset. Assuming that Y is 
**centered** or **demeaned**, we can define the covariance of Y as follows:

$$ \Sigma_{Y} = \frac{1}{n-1} Y^T Y $$

Given our transform \\(X V = Y \\) we can express the covariance of Y in terms V and X as follows:

$$ \begin{align}
\Sigma_{Y} &= \frac{1}{n-1} Y^T Y  \\\\
 &= \frac{1}{n-1}(XV)^T(XV) \\\\
 &= \frac{1}{n-1}V^T X^T X V \\\\
\end{align} $$

Let us define a new matrix A, which by definition is a `pxp` [symmetric matrix](https://en.wikipedia.org/wiki/Symmetric_matrix) as:

$$ A = \frac{1}{n-1} X^T X $$
 
We can therefore re-write our earlier expression for \\(\Sigma_{Y}\\) in terms of A as:

$$ \Sigma_{Y} = V^T A V $$

In the next section, we illustrate how we can choose \\(V\\) such that we diagonalize \\(\Sigma_{Y}\\). 

### Eigen Decomposition

Based on our earlier discussion, we know that we need to choose a transform matrix V such that \\(V^TAV = D\\) where \\(D\\) 
is a diagonal matrix. Given that \\(A\\) is symmetric, we know that we can factorize it using an [Eigendecomposition](https://en.wikipedia.org/wiki/Eigendecomposition_of_a_matrix) 
into an orthogonal matrix of its [eigenvectors](https://en.wikipedia.org/wiki/Eigenvalues_and_eigenvectors) and a diagonal 
matrix of its [eigenvalues](https://en.wikipedia.org/wiki/Eigenvalues_and_eigenvectors):

$$ A = Q \Lambda Q^{-1} $$

In this factorization the column vectors of \\(Q\\) are the eigenvectors of \\(A\\) and the diagonal elements of \\(\Lambda\\) 
are the eigenvalues of \\(A\\). Recall that an eigenvector of a matrix is one which is only scaled and not rotated when operated 
on by that matrix. This is otherwise stated as \\(Av = \lambda v \\) where \\(v\\) is an `px1` eigenvector of A and \\(\lambda\\) is 
the corresponding eigenvalue, which is a scalar. We can therefore expand this to say that \\(A Q = Q \Lambda \\) which then 
implies that \\(A = Q \Lambda Q^{-1}\\).
 
If we choose our matrix \\(V = Q\\) and noting that Q is an orthogonal matrix such that \\(Q^{-1} = Q^T \\) we can plug these back 
into the expression for the covariance matrix of Y to yield the following:

$$ \begin{align}
\Sigma_{Y} &= V^T A V \\\\
 &= V^T (V \Lambda V^{-1}) V \\\\
 &= (V^TV)\Lambda(V^{-1}V) \\\\
 &= (V^{-1}V)\Lambda(V^{-1}V) \\\\
 &= \Lambda \\\\
\end{align} $$

By choosing the transform matrix V to be the eigenvectors of \\(A \\) (which in essence is the covariance matrix of our data
on the assumption that \\(X\\) is centered), we end up diagonalizing the covariance matrix of the transformed dataset, which is 
the ultimate objective of our PCA. These eigenvectors essentially define the new basis along which variance in the data is maximised, 
and the corresponding eigenvalues define the magnitude of this variance. As noted earlier, the eigenvectors or principal components 
are usually ordered so that the first component accounts for the largest variance (ie the largest eigenvalue), and the last component 
accounts for the smallest variance (ie the smallest eigenvalue).

### Singular Value Decomposition

The previous section demonstrated that an eigen decomposition of the covariance matrix of \\(X\\) yields the set of eigenvectors \\(V\\) 
that define the principal axes of our data. When we transform our data using \\(V\\) the resulting dataset has a diagonal covariance
matrix which reflects that our new basis maximises the signal to noise ratio and/or removes any redundancy.

The Morpheus library supports solving PCA in this way, but by default, it performs a [Singular Value Decomposition](https://en.wikipedia.org/wiki/Singular_value_decomposition)
(SVD) of the **centered** or **demeaned** data in \\(X\\), as this generally offers better numerical stability and also tends to be faster
then an eigendecomposition of the covariance matrix of \\(X\\). An SVD of \\(X\\) on the assumption that \\(X\\) is centered yields

$$ X = U S V^T $$

where S is a diagonal matrix of singular values, and the columns of \\(U\\) and \\(V\\) are called the left-singular vectors and right-singular 
vectors of \\(X\\) , respectively. If we substitute the SVD decomposition into the expression for the covariance of \\(X\\) as below, we can 
see that the solution is essentially equivalent to above, however in this case the eigenvalues are equivalent to the square of the singular 
values divided by \\(n-1\\).

$$ \begin{align}
\Sigma_{X} &= \frac{1}{n-1} X^T X \\\\
 &= \frac{1}{n-1} (U S V^T)^T(U S V^T) \\\\
 &= \frac{1}{n-1} VSU^TUSV^T \\\\ 
 &= \frac{1}{n-1} VS^2V^T \\\\
\end{align} $$

## Example

### Data Model

In this example, we demonstrate the use of Principal Component Analysis as a dimensional reduction technique, and in particular 
we apply it to the problem of image compression. Consider the photo below of my dog who is called [Poppet](http://www.urbandictionary.com/define.php?term=poppet), 
which is 504 pixels wide and 360 pixels high. The pixels that make up this image can be thought of as a `360x504` matrix, where the elements 
represent the color of each pixel. It is most common in computer graphics to represent such an image using the [RGBA Color Space](https://en.wikipedia.org/wiki/RGBA_color_space)
where each pixel is defined by a [32-bit](https://en.wikipedia.org/wiki/32-bit) integer which has encoded within it four 8-bit 
values representing its red, green, blue and alpha intensity.  Since each component within the RGBA value is represented by an 
8-bit sequence, they have a range between 0 and 255 in base-10.

<div style="text-align:center;">
    <img src="../../images/pca/poppet.jpg" width="249" height="178"/>
</div>

We can load the target image into a Morpheus `DataFrame` of RGBA values using the code below. Here we initialize a frame of
integer values with the row and column count based on the image dimensions.

<?prettify?>
```java
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

URL url = getClass().getResource("/poppet.jpg");
BufferedImage image = ImageIO.read(url);
int rowCount = image.getHeight()
int colCount = image.getWidth();
DataFrame<Integer,Integer> rgbFrame = DataFrame.ofInts(rowCount, colCount, v -> {
    return image.getRGB(v.colOrdinal(), v.rowOrdinal());
});
```

Given that each pixel is represented by a 32-bit RGBA value, we can decompose the `360x504` matrix into a `360x504x4` cube of data, 
or a `360x504x3` cube if we ignore the alpha channel on the assumption that each pixel is 100% opaque (which is a reasonable assumption
in this case). In order to decompose the matrix into the red, green and blue components we need to perform some [bitwise operations](https://en.wikipedia.org/wiki/Bitwise_operation). 
In this example, we load the target image using `java.awt.image.BufferedImage` which exposes the 32-bit RGB values in the form 
illustrated below, where the first 8 most significant bits represent the alpha channel, the next 8 represent the red value, followed 
by green and then blue.

<div style="text-align:center;">
    <img src="../../images/pca/argb-channels.png"/>
</div>

To extract the 8-bit value representing the red intensity, we first need to shift our string of bits 16 places to the right so that 
the 8-bits representing the value of red appear in bit positions 0-7 (which before the shift represented the blue intensity). With our 
bit string now in this form, we can bitwise AND it with a base-10 value of 255 or `0xFF` in hexadecimal so that all bits in positions 
8-31 become zero, leaving only the value of our red intensity. Similarly, to extract the value of green, we right shift our RGBA bit 
string by 8, and then bitwise AND with `0xFF`. In the case of extracting blue, no bit shifting is required and we simply bitwise AND 
with `0xFF`. The code below generates 3 separate frames to capture the red, green and blue intensities by performing the bitwise 
operations just described.

<?prettify?>
```java
DataFrame<Integer,Integer> red = rgbFrame.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
DataFrame<Integer,Integer> green = rgbFrame.mapToDoubles(v -> (v.getInt() >> 8) & 0xFF);
DataFrame<Integer,Integer> blue = rgbFrame.mapToDoubles(v -> v.getInt() & 0xFF);
```

### Explained Variance

Now that we know how to decompose our image into three `360x504` frames representing the red, green and blue intensity of our image 
pixels, we can perform Principal Component Analysis on each `DataFrame`, and assess the results. Since PCA is all about transforming data 
into a new basis in which the variance is maximised, it is often useful to get a sense of how much of the variance in the data is explained 
by the first N components. The code below uses the `rgbFrame` initialized above, extracts the red, green and blue components, performs 
PCA on each of these frames, and then collects the percent of variance explained by the respective principal components. This data is 
then trimmed to only include the first 10 components, which is then plotted using a bar chart. Note that in this example we **transpose** 
the `DataFrame` before calling the `pca()` method as the Morpheus library assumes that the data is an `nxp` matrix where `n >= p`.

<?prettify?>
```java
URL url = getClass().getResource("/poppet.jpg");
DataFrame<Integer,Integer> rgbFrame = DataFrame.ofImage(url);
Range<Integer> rowKeys = Range.of(0, rgbFrame.rowCount());

DataFrame<Integer,String> result = DataFrame.ofDoubles(rowKeys, Array.of("Red", "Green", "Blue"));
Collect.<String,DataFrame<Integer,Integer>>asMap(mapping -> {
    mapping.put("Red", rgbFrame.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF));
    mapping.put("Green", rgbFrame.mapToDoubles(v -> (v.getInt() >> 8) & 0xFF));
    mapping.put("Blue", rgbFrame.mapToDoubles(v -> v.getInt() & 0xFF));
}).forEach((name, color) -> {
    color.transpose().pca().apply(true, model -> {
        DataFrame<Integer,Field> eigenFrame = model.getEigenValues();
        DataFrame<Integer,Field> varPercent = eigenFrame.cols().select(Field.VAR_PERCENT);
        result.update(varPercent.cols().mapKeys(k -> name), false, false);
        return Optional.empty();
    });
});

DataFrame<Integer,String> chartData = result.rows().select(c -> c.ordinal() < 10).copy();
Chart.of(chartData.rows().mapKeys(r -> String.valueOf(r.ordinal())), chart -> {
    chart.plot(0).withBars(0d);
    chart.style("Red").withColor(Color.RED);
    chart.style("Green").withColor(Color.GREEN);
    chart.style("Blue").withColor(Color.BLUE);
    chart.axes().range(0).label().withText("Percent of Variance");
    chart.axes().domain().label().withText("Principal Component");
    chart.title().withText("Eigen Spectrum (Percent of Explained Variance)");
    chart.legend().on().bottom();
    chart.show();
});
```

We can see from this chart that in the case of the red frame, the first principal component explains around 45% of the variance, for 
green it is just under 35% and for blue it is just over 25%. The percentage of the variance explained by subsequent components drops 
off fairly monotonically, and by the time we get to the fifth component, only about 5% of the variance is captured for each of the
colors.

<div style="text-align:center;">
    <img src="../../images/pca/poppet-explained-variance.png"/>
</div>

### Dimensional Reduction

As per the earlier discussion on PCA theory, we established that the eigenvectors of the covariance matrix of our data are the principal
axes, and when combined as the columns of a matrix, serve as a transformation into the new basis. If we let \\(X_i\\) represent our `nxp` 
input dataset of either red, green or blue intensities, and \\(V_i\\) our matrix of `pxp` eigenvectors of the covariance matrix of \\(X_i\\), 
we can write the transform as follows (where \\(i\\) is either red, green or blue).

$$ X_i V_i = Y_i $$
 
This projection of the original data onto the new basis are called the **principal component scores**, and are directly accessible from
the Morpheus interface named `DataFramePCA.Model` via the `getScores()` method. The following code demonstrates how to access these scores,
and here we assert our expectation of the dimensions of these scores being `nxp` or `504x360` in this case (since we take the transpose
of the image).

<?prettify?>
```java
URL url = getClass().getResource("/poppet.jpg");
DataFrame<Integer,Integer> image = DataFrame.ofImage(url).transpose();
DataFrame<Integer,Integer> red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
red.pca().apply(true, model -> {
    DataFrame<Integer,Integer> scores = model.getScores();
    Assert.assertEquals(scores.rowCount(), 504);
    Assert.assertEquals(scores.colCount(), 360);
    return Optional.empty();
});
```


Since we know that \\(V_i\\) is an orthogonal matrix by design, once we have transformed to the new basis of \\(Y_i\\) we can transform
back to the original basis by taking the dot product of \\(Y_i\\) with \\(V_i^T\\) as follows:

$$ X_i = Y_i V_i^T $$

The eigenvectors that constitute the columns of \\(V_i\\) are arranged so that the first column is associated with the highest eigenvalue, 
and the last column with the lowest eigenvalue (recall that the eigenvalue represents the variance in the direction of the corresponding 
eigenvector). Given that much of the variance in the data will be explained by the leading eigenvectors, we consider truncating \\(V_i\\) 
by only retaining the first N columns. In this case, we can re-write the above expression using a tilde over \\(V_i\\) and \\(Y_i\\) to 
indicate that some information has been lost in this new transform due to the truncation of \\(V_i\\).

$$ X_i \tilde{V_i} = \tilde{Y_i} $$ 

The Morpheus API provides an over-loaded `getScores()` method on `DataFramePCA.Model` where we can generate \\(\tilde{Y_i}\\) by selecting
only the first `j` columns of \\(V_i\\) as shown below. In this case we assert that the expected dimensions of the scores is `nxk` rather
than `nxp`, where `k` is the number of components to include (below we use `k=10`).

<?prettify?>
```java
URL url = getClass().getResource("/poppet.jpg");
DataFrame<Integer,Integer> image = DataFrame.ofImage(url).transpose();
DataFrame<Integer,Integer> red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
red.pca().apply(true, model -> {
    DataFrame<Integer,Integer> scores = model.getScores(10);
    Assert.assertEquals(scores.rowCount(), 504);
    Assert.assertEquals(scores.colCount(), 10);
    return Optional.empty();
});
```

Given the truncated scores in the form of \\(\tilde{Y_i}\\), it turns out that we can project these scores back onto the original basis 
using \\(\tilde{V_i}^T\\) much in the same way as described earlier. If we right multiply \\(\tilde{Y_i}\\) by \\(\tilde{V_i}^T\\)
we get back `nxp` data since \\(\tilde{Y_i}\\) is `nxk` and \\(\tilde{V_i}^T\\) is `kxp`, and yields an estimate of our original data
we now call \\(\tilde{X_i}\\) 

$$ \tilde{X_i} = \tilde{Y_i} \tilde{V_i}^T = X_i \tilde{V_i} \tilde{V_i}^T $$ 

The Morpheus API provides a convenient API to generate \\(\tilde{X_i}\\) based on a specified number of components, `k`. The following
code shows how to access the projection of our original data using only the first `k=10` components, and we assert that the dimensions
of this data matches our original image, namely `504x360` (since we transpose the image for reasons discussed earlier).

<?prettify?>
```java
URL url = getClass().getResource("/poppet.jpg");
DataFrame<Integer,Integer> image = DataFrame.ofImage(url).transpose();
DataFrame<Integer,Integer> red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
red.pca().apply(true, model -> {
    DataFrame<Integer,Integer> projection = model.getProjection(10);
    Assert.assertEquals(projection.rowCount(), 504);
    Assert.assertEquals(projection.colCount(), 360);
    return Optional.empty();
});
```

We have established that it is possible to reconstitute an estimate of our original data \\(X_i\\), which we called \\(\tilde{X_i}\\), 
from the principal component scores and a subset of the principal axes associated with the highest variance. The next question is how 
many columns of \\(V_i\\) need to be retained to ensure \\(\tilde{X_i}\\) is a reasonable representation of the original data? There 
is no hard rule in this regard, however a common rule of thumb is to select enough components to ensure that 90% of the variance is 
captured. Having said that, each problem will be unique, and it will often be useful to generate an eigen spectrum plot as shown above 
to draw some conclusion as to a reasonable initial estimate.

In the case of our image of Poppet, we will demonstrate the effect of retaining an increasing number of principal axes in \\(V_i\\) 
to compute principal component scores, and then to project this back onto the original basis. The images below show a range of scenarios 
where we project the image using only 5 components all the way through to 70 components. Note that this is still a small subset of the 
total number of components, namely 360 in this case, but it is clear that once we include up to 50 components, the transformed image 
is almost indistinguishable from the original, at least to the human eye.

<table>
    <tr>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-5.jpg"/><br>
            <span>5 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-10.jpg"/><br>
            <span>10 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-15.jpg"/><br>
            <span>15 Principal Components</span>
        </td>
    </tr>
    <tr>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-20.jpg"/><br>
            <span>20 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-25.jpg"/><br>
            <span>25 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-30.jpg"/><br>
            <span>30 Principal Components</span>
        </td>
    </tr>
    <tr>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-35.jpg"/><br>
            <span>35 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-40.jpg"/><br>
            <span>40 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-45.jpg"/><br>
            <span>45 Principal Components</span>
        </td>
    </tr>
    <tr>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-50.jpg"/><br>
            <span>50 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-55.jpg"/><br>
            <span>55 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-60.jpg"/><br>
            <span>60 Principal Components</span>
        </td>
    </tr>
    <tr>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-65.jpg"/><br>
            <span>65 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-70.jpg"/><br>
            <span>70 Principal Components</span>
        </td>
        <td class="dog">
            <img class="dog" src="../../images/pca/poppet-360.jpg"/><br>
            <span>360 Principal Components</span>
        </td>
    </tr>
</table>

The final image in the table above is essentially the same as the original since we retain all components and so \\(V_i {V_i}^T = I \\)
given that we know \\(V_i\\) is an orthogonal matrix by design. The code to generate this array of images is shown below. Here we load the 
original image into a Morpheus `DataFrame`, and then proceed to decompose it into red, green and blue components, perform PCA on each color, 
project the image as described above using only a subset of the principal axes associated with highest variance, and then record the resulting 
projection back out as an image file. 

<?prettify?>
```java
//Load image from classpath
URL url = getClass().getResource("/poppet.jpg");

//Re-create PCA reduced image while retaining different number of principal components
Array.of(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 360).forEach(nComp -> {

    //Initialize the **transpose** of image as we need nxp frame where n >= p
    DataFrame<Integer,Integer> rgbFrame = DataFrame.ofImage(url).transpose();

    //Create 3 frames from RGB data, one for red, green and blue
    DataFrame<Integer,Integer> red = rgbFrame.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
    DataFrame<Integer,Integer> green = rgbFrame.mapToDoubles(v -> (v.getInt() >> 8) & 0xFF);
    DataFrame<Integer,Integer> blue = rgbFrame.mapToDoubles(v -> v.getInt() & 0xFF);

    //Perform PCA on each color frame, and project using only first N principal components
    Stream.of(red, green, blue).parallel().forEach(color -> {
        color.pca().apply(true, model -> {
            DataFrame<Integer,Integer> projection = model.getProjection(nComp);
            projection.cap(true).doubles(0, 255);  //cap values between 0 and 255
            color.update(projection, false, false);
            return null;
        });
    });

    //Apply reduced RBG values onto the original frame so we don't need to allocate memory
    rgbFrame.applyInts(v -> {
        int i = v.rowOrdinal();
        int j = v.colOrdinal();
        int r = (int)red.data().getDouble(i,j);
        int g = (int)green.data().getDouble(i,j);
        int b = (int)blue.data().getDouble(i,j);
        return ((0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
    });

    //Create reduced image from **transpose** of the DataFrame to get back original orientation
    int width = rgbFrame.rowCount();
    int height = rgbFrame.colCount();
    BufferedImage transformed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    rgbFrame.forEachValue(v -> {
        int i = v.colOrdinal();
        int j = v.rowOrdinal();
        int rgb = v.getInt();
        transformed.setRGB(j, i, rgb);
    });

    try {
        File outputfile = new File("/Users/witdxav/temp/poppet-" + nComp + ".jpg");
        outputfile.getParentFile().mkdirs();
        ImageIO.write(transformed, "jpg", outputfile);
    } catch (Exception ex) {
        throw new RuntimeException("Failed to record image result", ex);
    }
});
```

### Compression Story

The dimensions of our original input data \\(X_i\\) is `504x360` which generates a covariance matrix with dimensions `360x360`. 
Performing PCA on this data yields a set of `360` eigenvectors each of the same length, implying that the non-truncated version of 
\\(V_i\\) is also `360x360`. If we decide to only keep the first `k` columns of \\(V_i\\) to create \\(\tilde{V_i}\\), then the resulting 
dimensions of \\(\tilde{Y_i}\\) will be `504xk`. If `k` can be significantly smaller than `360` (the height of the original image) then 
\\(\tilde{Y_i}\\) would require much less storage space. We obviously also need to store \\(\tilde{V_i}\\) so that we can reconstitute 
our estimate of the data \\(\tilde{X_i}\\), but the expectation is that `k` can be small enough so that the storage required for two 
smaller matrices is less than that required for the original image. 

In our example, the original image requires `181,440` 32-bit RGBA values given it has dimensions of `504x360` pixels. The table below
summarizes the total number of elements required to store \\(\tilde{Y_i}\\) and \\(\tilde{V_i}\\) for various values of k ranging
from 5 through 60. We need to multiply this by 3 since we need to store a red, green and blue version of these matrices. The final
column indicates the percent reduction on the original number of elements we achieve, and since the image reconstituted by retaining
only the first 45 components is almost indistinguishable from the original, we can achieve 35% compression in that case. 
 
| k   | \\(\tilde{Y_i}\\) | \\(\tilde{V_i}\\) | Total    | Total x 3 | Compression |
|-----|:------------------|:------------------|:---------|:----------|:------------|
|  5  |  504x5  =  2,520  |  360x5  =  1,800  |   4,320  |   12,960  |      92.86% |
| 10  |  504x10 =  5,040  |  360x10 =  3,600  |   8,640  |   25,920  |      85.71% |  
| 15  |  504x15 =  7,560  |  360x15 =  5,400  |  12,960  |   38,880  |      78.57% |
| 20  |  504x20 = 10,080  |  360x20 =  7,200  |  17,280  |   51,840  |      71.43% |
| 25  |  504x25 = 12,600  |  360x25 =  9,000  |  21,600  |   64,800  |      64.29% |
| 30  |  504x30 = 15,120  |  360x30 = 10,800  |  25,920  |   77,760  |      57.14% |
| 35  |  504x35 = 17,640  |  360x35 = 12,600  |  30,240  |   90,720  |      50.00% |
| 40  |  504x40 = 20,160  |  360x40 = 14,400  |  34,560  |  103,680  |      42.86% |
| 45  |  504x45 = 22,680  |  360x45 = 16,200  |  38,880  |  116,640  |      35.71% |
| 50  |  504x50 = 25,200  |  360x50 = 18,000  |  43,200  |  129,600  |      28.57% |
| 55  |  504x55 = 27,720  |  360x55 = 19,800  |  47,520  |  142,560  |      21.43% |
| 60  |  504x60 = 30,240  |  360x60 = 21,600  |  51,840  |  155,520  |       7.14% |

