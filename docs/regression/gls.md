## Generalized Least Squares (GLS)

### Introduction

One of the [Gauss Markov](ols/#gauss-markov-assumptions) assumptions that has to be met in order for [OLS](ols/) to be the Best, 
Linear, Unbiased Estimator (BLUE) is that of **spherical errors**. More specifically, the assumption states that the error or disturbance 
term in the model must exhibit uniform variance (i.e. [homoscedasticity](https://en.wikipedia.org/wiki/Homoscedasticity)), and also that 
there is no [autocorrelation](https://en.wikipedia.org/wiki/Autocorrelation) between errors.

In the real world, this assumption will not always hold, and errors may exhibit [heteroscedasticity](https://en.wikipedia.org/wiki/Heteroscedasticity) 
or autocorrelation or both. When the errors in a regression show no serial correlation but are heteroscedastic, then a [Weighted Least Squares](wls/) 
(WLS) model should be used, whereas if correlation is present a [Generalized Least Squares](https://en.wikipedia.org/wiki/Generalized_least_squares) (GLS)
model is appropriate. WLS is in fact a special case of GLS where the the lack of autocorrelation implies that the off diagonal terms of the 
[covariance matrix](https://en.wikipedia.org/wiki/Covariance_matrix) of the errors are zero, thus simplifying the analysis.

The following section covers some of the theory behind GLS, provides an example of how to apply it with the Morpheus API, and then goes
onto demonstrate empirically the [unbiased](https://en.wikipedia.org/wiki/Bias_of_an_estimator), [consistent](https://en.wikipedia.org/wiki/Consistent_estimator) 
and [efficient](https://en.wikipedia.org/wiki/Efficient_estimator) nature of the estimator.

### Theory

A Generalized Least Squares model is based on the usual linear equation, however we now assume that the error term has a variance structure 
that is described by a covariance matrix \\(\Omega\\), which differs from the OLS model which assumes white noise in the form of \\(\sigma^2 I\\).

$$ Y = X \beta + \epsilon \ \ \ \ where \ E[\epsilon]=0 \ \ \ \ Var(\epsilon) = \sigma^2 \Omega  $$ 

Given that the presence of serial correlation and possibly heteroscedasticity is a violation of one of the [Gauss Markov](ols/#gauss-markov-assumptions)
assumptions, OLS is no longer BLUE. To address this, we follow the same strategy as in [Weighted Least Squares](wls/#theory), where we consider the 
existence of an unknown matrix P that when applied to both sides of the model equation, transforms it in such a way that the transformed system 
exhibits **spherical errors**. If such a matrix P exists, we can apply OLS to the trasnsformed system given that it satisfies the Gauss Markov 
assumptions. Consider the transformed system below:

$$ P Y = P X \beta + P \epsilon \ \ \ \ where \ E[P\epsilon]=0 \ \ \ \ Var(P\epsilon) = \sigma^2 I $$

The next step is to solve for P in terms of something we can observe or estimate. As it turns out, it is possible to solve for P in terms of \\(\Omega\\) 
which itself can be estimated from the sample data of whatever problem happens to be under investigation.  Based on an assumption that P is a symmetric 
matrix and therefore \\(P^T = P\\), we can express \\(P\\) in terms of \\(\Omega\\) as follows:

$$ \begin{align}
Var(P \epsilon) & = \sigma^2 I \\\\
P Var(\epsilon) P^T & = \sigma^2 I \\\\
\sigma^2 P \Omega P^T & = \sigma^2 I \\\\
P \Omega P^ T & = I \\\\
\Omega P^T & = P^{-1} \\\\
\Omega & = (P^2)^{-1} \\\\
P & = \Omega^{-\frac{1}{2}}
\end{align} $$

This analysis suggests that the transform matrix P is in fact equal to \\(\Omega^{-\frac{1}{2}}\\), and when applied to our original model,
should yield a new model with spherical errors against which we can apply a standard OLS estimator. Let us re-write the transformed model 
in terms of Z, B and E as follows:

$$ Z = B \beta + E \ \ \ \  where \ Z = P Y, \ \ B = PX, \ \ and \\ E = P \epsilon $$
 
We can use the standard OLS estimator for this since we know the residuals constitute white noise:

$$ \hat{\beta}_{ols} = (B^T B)^{-1} B^T Z $$

Knowing that \\(B=PX\\) we can write the GLS estimator as follows:

$$ \hat{\beta}_{gls} = (X^T P^T P X)^{-1} X^T P^T P Y  $$

Substituting \\(\Omega\\) for P:

$$ \hat{\beta}_{gls} = (X^T \Omega^{-1} X)^{-1} X^T \Omega^{-1} Y $$

Given the \\(\hat{\beta}_{gls}\\) expression above, it is clear that some estimate of \\(\Omega\\) is required. Note that the absolute scale
of \\(\Omega\\) is not required to be known up front since the scale comes from \\(\sigma^2\\) in the expression \\(Var(\epsilon)=\sigma^2\Omega\\).
If the errors are uncorrelated but exhibit non-uniform variance, \\(\Omega\\) is a diagonal matrix such that all off diagonal terms are zero, and
the problem becomes WLS. If the errors are correlated, which is often the case in time series data, the off diagonal terms will be non-zero.

### Example

To demonstrate a GLS regression using the Morpheus API, we will define a data generating function which we can use to create a sample dataset
with the appropriate characteristics (i.e. serially correlated errors in a linear regression). For convenience, we will limit the dataset
to two dimensions so that the results are easy to visualize. Furthermore, to make our life easier in the context of this example, we will 
enforce a correlation structure in the error term, namely that of a [autoregressive](https://en.wikipedia.org/wiki/Autoregressive_model)
process of order 1, or AR(1) for short. Knowing this upfront makes the estimation of \\(\Omega\\) easy. Let us define our data generating
process as follows:

$$ y_{t} = \alpha + \beta x_{t} + u_{t} $$

$$ u_{t} = \rho u_{t-1} + \epsilon_{t} $$

Here we impose a structure on the error term of the form dictated by the second equation, which suggests that an error at time \\(t\\) is equal 
to the prior error multiplied by some correlation coefficient \\(\rho\\) where \\(|\rho|<1\\) plus white noise in the form of \\(\epsilon\\) 
which is assumed to be \\(N(0,\sigma^2)\\). The code below defines a function that is parameterized as per the above equations, and returns a 
`DataFrame` with two columns containing the X and Y values output by this model.

<?prettify?>
```java
/**
 * Returns a DataFrame of X & Y values based on the coefficients provided.
 * Noise is added to the dependent variable based on an AR(1) process
 * @param alpha     the intercept term for population process
 * @param beta      the slope term for population process
 * @param rho       the AR(1) coefficient used to generate serially correlated errors
 * @param sigma     the std deviation of the normal distribution for noise
 * @param seed      if true, initialize with a fixed seed for reproducible results
 * @return          the frame of XY values with serially correlated residuals
 */
DataFrame<Integer,String> sample(double alpha, double beta, double rho, double sigma, int n, boolean seed) {
    final double startX = 1d;
    final double stepX = 0.5d;
    final RandomGenerator rand = seed ? new Well19937c(1234565) : new Well19937c();
    final RealDistribution noise = new NormalDistribution(rand, 0, sigma);
    final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
    final Array<Integer> rowKeys = Range.of(0, n).toArray();
    return DataFrame.of(rowKeys, String.class, columns -> {
        columns.add("X", xValues);
        columns.add("Y", Array.of(Double.class, n).applyDoubles(v -> {
            final double xValue = xValues.getDouble(v.index());
            final double yFitted = alpha + beta * xValue;
            if (v.index() == 0) return yFitted + noise.sample();
            else {
                final double priorX = xValues.getDouble(v.index()-1);
                final double priorY = v.array().getDouble(v.index()-1);
                final double priorError = priorY - (alpha + beta * priorX);
                final double error = rho * priorError + noise.sample();
                return yFitted + error;
            }
        }));
    });
}
```

To get a sense of what this data looks like, we can generate 4 random samples and plot them as below, while fitting an OLS regression model 
to the data. The serial correlation in the residuals should be fairly obvious given that we can see a sequence of errors above the regression 
line followed by a sequence below the line, which clearly is not typical of white noise.

<div style="float:left;width:50%;">
    <img class="chart" src="../../images/gls/gls-sample-0.png"/>
</div>
<div style="float:left;width:50%;">
    <img class="chart" src="../../images/gls/gls-sample-1.png"/>
</div>
<div style="float:left;width:50%;">
    <img class="chart" src="../../images/gls/gls-sample-2.png"/>
</div>
<div style="float:left;width:50%;">
    <img class="chart" src="../../images/gls/gls-sample-3.png"/>
</div>

The code to generate these plots is as follows:

<?prettify?>
```java
final int n = 100;
final double rho = 0.5d;
final double beta = 4d;
final double alpha = 20d;
final double sigma = 10d;
Chart.show(2, IntStream.range(0, 4).mapToObj(i -> {
    DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, false);
    String title = "Sample %s Dataset, Beta: %.2f Alpha: %.2f";
    String subtitle = "Parameter estimates, Beta^: %.3f, Alpha^: %.3f";
    DataFrameLeastSquares<Integer,String> ols = frame.regress().ols("Y", "X", true, Optional::of).get();
    double betaHat = ols.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
    double alphaHat = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
    return Chart.create().withScatterPlot(frame, false, "X", chart -> {
        chart.title().withText(String.format(title, i, beta, alpha));
        chart.title().withFont(new Font("Arial", Font.BOLD, 14));
        chart.subtitle().withText(String.format(subtitle, betaHat, alphaHat));
        chart.plot().style("Y").withColor(Color.RED).withPointsVisible(true);
        chart.plot().trend("Y");
    });
}));
```

In the real world we would obviously not know if there was serial correlation in the residuals, and if there was, what type of model might
be appropriate for modeling it. Other than looking at a scatter plot, there are various ways of checking for the presence of serial correlation.
One commonly used technique is to compute the [Durbin Watson](https://en.wikipedia.org/wiki/Durbin%E2%80%93Watson_statistic) test statistic, 
which by default is included in the model output generated by the Morpheus library, and is defined as:

$$ d = \frac{\sum_{t=2}^T (u_{t} - u_{t-1}^2)}{\sum_{t=1}^T u_{t}^2} $$

The value of this statistic lies between 0 and 4, and if it is substantially less than 2, there is evidence for positive serial correlation. 
For more details on this statistic, consult the above link. The code below runs an OLS regression given a sample of our data and prints the 
model results to standard out. The Durbin Watson statistic comes out to be **1.0603** which is highly suggestive that there is positive serial 
correlation in the residuals.

<?prettify?>
```java
int n = 100;
double rho = 0.5d;
double beta = 4d;
double alpha = 20d;
double sigma = 10d;
DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, true);
frame.regress().ols("Y", "X", true, model -> {
    System.out.println(model);
    return Optional.empty();
});
```

<pre class="frame">
=============================================================================================
                                  Linear Regression Results                                                            
=============================================================================================
Model:                                   OLS    R-Squared:                            0.9583
Observations:                            100    R-Squared(adjusted):                  0.9579
DF Model:                                  1    F-Statistic:                       2253.2056
DF Residuals:                             98    F-Statistic(Prob):                       0E0
Standard Error:                      11.5149    Runtime(millis)                           51
Durbin-Watson:                        1.0603                                                
=============================================================================================
   Index    |  PARAMETER  |  STD_ERROR  |  T_STAT   |   P_VALUE   |  CI_LOWER  |  CI_UPPER  |
---------------------------------------------------------------------------------------------
 Intercept  |    26.3017  |     2.3551  |   11.168  |  3.607E-19  |   21.6281  |   30.9753  |
         X  |     3.7871  |     0.0798  |  47.4679  |  1.955E-69  |    3.6288  |    3.9454  |
=============================================================================================
</pre>

Once we have established a belief that the residuals are serially correlated, and therefore OLS is no longer BLUE, we then need to understand 
the nature of the serial correlation. In this example we know the structure comes from an AR(1) process, but in reality some analysis of the residuals 
will be required in order to come up with a reasonable model, and hence some estimation of the covariance matrix \\(\Omega\\). A common first task would 
be to plot the autocorrelation function of the residuals, which is a visualization usually referred to as a [correlogram](https://en.wikipedia.org/wiki/Correlogram),
and is illustrated below for this example. The peak at lag 1 clearly breaches the upper bound of the 95% confidence interval represented by the dashed blue 
line (the bar at lag 0 is by definition equal to 1).

<p align="center">
    <img class="chart" src="../../images/gls/gls-acf.png"/>
</p>

The code to generate this plot is shown below, which effectively runs an OLS regression and then passes the resulting model object to the chart 
`acf()` function, which is an abbreviation for Autocorrelation Function. In addition, the third argument to the `acf()` method is the significance 
level to use in order to generate the confidence intervals for the ACF. In this case, we chose a 5% level of significance, resulting in the dashed 
blue lines representing the 95% confidence intervals.

<?prettify?>
```java
final int n = 200;
final double rho = 0.5d;
final double beta = 4d;
final double alpha = 20d;
final double sigma = 10d;
final DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, true);
frame.regress().ols("Y", "X", true, model -> {
    Chart.create().withAcf(model, frame.rowCount()/2, 0.05d, chart -> {
        final double rhoHat = model.getResiduals().colAt(0).stats().autocorr(1);
        chart.title().withText("Residual Autocorrelation Plot");
        chart.subtitle().withText(String.format("Autocorrelation Lag 1 = %.3f", rhoHat));
        chart.show(800, 300);
    });
    return Optional.empty();
});
```

We have now established that there is serial correlation in the residuals based on the Durbin Watson test statistic, and we can reasonably
assume that an AR(1) process is an appropriate model for the residuals based on our observations in the ACF plot. Given this information
we now have to estimate \\(\Omega\\) which is the covariance matrix of the residuals across all lags in the sample. As it turns out,
an AR(1) process has a well defined correlation structure which is described in more detail [here](https://onlinecourses.science.psu.edu/stat510/node/60), 
and suggests that the correlation between two observations k periods apart is equal to \\(\rho^k\\). 

The elements of the \\(\Omega\\) matrix can therefore be easily computed since the entry in the \\(i^{th}\\) row and \\(j^{th}\\) column will 
simply be \\(\rho^{|i-j|}\\). The code below returns a `DataFrame` to represent \\(\Omega\\) given the value of \\(\rho\\), which should be the 
autocorrelation coefficient at lag 1 calculated from the residuals, and the sample size of the data since \\(\Omega\\) has `nxn` dimensions.

<?prettify?>
```java
/**
 * Returns the correlation matrix omega for an AR(1) process
 * @param size      the size for the correlation matrix
 * @param autocorr  the auto correlation coefficient
 * @return          the newly created correlation matrix
 */
private DataFrame<Integer,Integer> createOmega(int size, double autocorr) {
    final Range<Integer> keys = Range.of(0, size);
    return DataFrame.ofDoubles(keys, keys, v -> {
        return Math.pow(autocorr,  Math.abs(v.rowOrdinal() - v.colOrdinal()));
    });
}
```

Now that we have an estimate for \\(\Omega\\) we can run a GLS regression which is illustrated by the code and results below. While we specify
our value of \\(\rho\\) upfront in order to generate our data sample, we first run an OLS regression so that we can calculate an estimate
of \\(\hat{\rho}\\) based on the autocorrelation coefficient of the residuals with lag 1. This \\(\hat{\rho}\\) estimate is then used to construct \\(\Omega\\)
which we pass to the `gls()` method on the `DataFrameRegression` interface.

<?prettify?>
```java
int n = 100;
double rho = 0.5d;
double beta = 4d;
double alpha = 20d;
double sigma = 10d;
DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, true);
frame.regress().ols("Y", "X", true, ols -> {
    final double rhoHat = ols.getResiduals().colAt(0).stats().autocorr(1);
    final DataFrame<Integer,Integer> omega = createOmega(n, rhoHat);
    frame.regress().gls("Y", "X", omega, true, gls -> {
        System.out.println(gls);
        return Optional.empty();
    });
    return Optional.empty();
});
```

<pre class="frame">
=============================================================================================
                                  Linear Regression Results                                                            
=============================================================================================
Model:                                   GLS    R-Squared:                            0.9080
Observations:                            100    R-Squared(adjusted):                  0.9071
DF Model:                                  1    F-Statistic:                        967.2651
DF Residuals:                             98    F-Statistic(Prob):                       0E0
Standard Error:                      11.5135    Runtime(millis)                           49
Durbin-Watson:                        1.8814                                                
=============================================================================================
   Index    |  PARAMETER  |  STD_ERROR  |  T_STAT   |   P_VALUE   |  CI_LOWER  |  CI_UPPER  |
---------------------------------------------------------------------------------------------
 Intercept  |    26.3744  |     3.8188  |   6.9065  |   5.01E-10  |   18.7961  |   33.9527  |
         X  |     3.7934  |     0.1288  |  29.4463  |  1.796E-50  |    3.5378  |    4.0491  |
=============================================================================================
</pre>

The coefficient estimates from the GLS regression are very close to that produced by the OLS model, however the standard errors are higher for
GLS, and the 95% confidence intervals substantially wider. One of the known problems of OLS in the presence of serially correlated errors is
that it yields downwardly biased estimates of the standard errors, and therefore statistical inference from these is unreliable. In this
particular problem, the OLS parameter confidence interval includes the known population values, but the magnitude of the noise that drives
the residuals is relatively small, so this will not always be the case.

### Unbiasedness

As per the [OLS](ols/#unbiasedness) and [WLS](wls/#unbiasedness) examples, here we attempt to demonstrate empirically the unbiased nature of 
the GLS estimator. We have already introduced our data generating process earlier (see `sample()` function above), which can be used to manufacture 
many data samples on which we can fit a GLS model, while collecting all the parameter estimates for each run. The code below generates 100,000
samples of data with \\(n=100\\), runs a GLS regression on each, and then plots the frequency distribution of the slope and intercept estimates. 
As expected, we get a bell shaped normal distribution centered on the known population values for both the slope and intercept parameters.

<?prettify?>
```java
final int n = 100;
final double rho = 0.5d;
final double beta = 4d;
final double alpha = 20d;
final double sigma = 20d;
final int regressionCount = 100000;
Range<Integer> rows = Range.of(0, regressionCount);
Array<String> columns = Array.of("Beta", "Alpha");
DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

//Run regressions in parallel to leverage all 4 cores...
results.rows().parallel().forEach(row -> {
    final DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, false);
    frame.regress().ols("Y", "X", true, ols -> {
        final double rhoHat = ols.getResiduals().colAt(0).stats().autocorr(1);
        final DataFrame<Integer,Integer> omega = createOmega(frame.rowCount(), rhoHat);
        frame.regress().gls("Y", "X", omega, true, model -> {
            final double alphaHat = model.getInterceptValue(Field.PARAMETER);
            final double betaHat = model.getBetaValue("X", Field.PARAMETER);
            row.setDouble("Alpha", alphaHat);
            row.setDouble("Beta", betaHat);
            return Optional.empty();
        });
        return Optional.empty();
    });
});

Array.of("Beta", "Alpha").forEach(coeff -> {
    final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
    Chart.create().withHistPlot(coeffResults, 250, chart -> {
        String title = "%s Histogram of %s GLS regressions, Rho = %.3f";
        String subtitle = "%s estimate unbiasedness, Actual: %.2f, Mean: %.2f, Variance: %.2f";
        double actual = coeff.equals("Beta") ? beta : alpha;
        double estimate = coeffResults.colAt(coeff).stats().mean();
        double variance = coeffResults.colAt(coeff).stats().variance();
        Color color = coeff.equals("Beta") ? new Color(255, 100, 100) : new Color(102, 204, 255);
        chart.plot().style(coeff).withColor(color);
        chart.plot().axes().domain().label().withText(coeff + " Estimate");
        chart.title().withText(String.format(title, coeff, regressionCount, rho));
        chart.subtitle().withText(String.format(subtitle, coeff, actual, estimate, variance));
        chart.show(700, 400);
    });
});
```

<p align="center">
    <img class="chart" src="../../images/gls/gls-beta-unbiasedness.png"/>
    <img class="chart" src="../../images/gls/gls-alpha-unbiasedness.png"/>
</p>


### Efficiency

A more efficient estimator implies one with lower [sampling error](https://en.wikipedia.org/wiki/Sampling_error) in the parameter estimates 
than other less efficient estimators. In the case of the [WLS](wls/#efficiency) example, the relative efficiency versus OLS in the presence 
of [heteroscedastic](https://en.wikipedia.org/wiki/Heteroscedasticity) errors was fairly stark. As it turns out, for the GLS data generating 
process introduced earlier (see `sample()` function above), the comparative efficiency with OLS is only marginally better. In this example, 
I have therefore increased the value of \\(\sigma\\) to 20 and the value of \\(\rho\\) to 0.8 in order to get a noticeable difference in the 
resulting frequency distributions.

The code below again generates 100,000 data samples and runs both OLS and GLS regressions on these. In each case the OLS residuals
are used to compute the autocorrelation coefficient with lag 1 which serves as the \\(\rho\\) value that is then used to create the
\\(\Omega\\) matrix for the GLS regression. Both the OLS and GLS parameters are captured in a `DataFrame` and the results are used
to generate the frequency distributions below. The story is slightly less compelling than for the WLS example, however the superior 
efficiency of GLS in this case is still apparent.

<?prettify?>
```java
final int n = 100;
final double rho = 0.8d;
final double beta = 4d;
final double alpha = 20d;
final double sigma = 20d;
final int regressionCount = 100000;
Range<Integer> rows = Range.of(0, regressionCount);
Array<String> columns = Array.of("Beta(OLS)", "Alpha(OLS)", "Beta(GLS)", "Alpha(GLS)");
DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

//Run regressions in parallel to leverage all 4 cores...
results.rows().parallel().forEach(row -> {
    DataFrame<Integer,String> data = sample(alpha, beta, rho, sigma, n, false);
    data.regress().ols("Y", "X", true, ols -> {
        final double alphaHatOls = ols.getInterceptValue(Field.PARAMETER);
        final double betaHatOls = ols.getBetaValue("X", Field.PARAMETER);
        row.setDouble("Alpha(OLS)", alphaHatOls);
        row.setDouble("Beta(OLS)", betaHatOls);
        final double rhoHat = ols.getResiduals().colAt(0).stats().autocorr(1);
        final DataFrame<Integer,Integer> omega = createOmega(n, rhoHat);
        data.regress().gls("Y", "X", omega, true, gls -> {
            double alphaHat = gls.getInterceptValue(Field.PARAMETER);
            double betaHat = gls.getBetaValue("X", Field.PARAMETER);
            row.setDouble("Alpha(GLS)", alphaHat);
            row.setDouble("Beta(GLS)", betaHat);
            return Optional.empty();
        });
        return Optional.empty();
    });
});

Array.of("Alpha", "Beta").forEach(coeff -> {
    final String olsKey = coeff + "(OLS)";
    final String glsKey = coeff + "(GLS)";
    final DataFrame<Integer,String> data = results.cols().select(olsKey, glsKey);
    Chart.create().withHistPlot(data, 350, chart -> {
        double meanOls = results.colAt(olsKey).stats().mean();
        double stdOls = results.colAt(olsKey).stats().stdDev();
        double meanWls = results.colAt(glsKey).stats().mean();
        double stdWls = results.colAt(glsKey).stats().stdDev();
        double coeffAct = coeff.equals("Alpha") ? alpha : beta;
        String title = "%s Histogram from %s OLS & GLS Regressions (n=%s)";
        String subtitle = "Actual: %.4f, Mean(OLS): %.4f, Std(OLS): %.4f, Mean(GLS): %.4f, Std(GLS): %.4f";
        chart.title().withText(String.format(title, coeff, regressionCount, n));
        chart.title().withFont(new Font("Arial", Font.BOLD, 15));
        chart.subtitle().withText(String.format(subtitle, coeffAct, meanOls, stdOls, meanWls, stdWls));
        chart.plot().axes().domain().label().withText(coeff + " Estimates");
        chart.legend().on().bottom();
        chart.show(700, 400);
    });
});
```

<p align="center">
    <img class="chart" src="../../images/gls/gls-beta-efficiency.png"/>
    <img class="chart" src="../../images/gls/gls-alpha-efficiency.png"/>
</p>

### Consistency

Finally we consider the consistency of the GLS estimator. A consistent estimator is one where the variance in the estimates decreases
as the sample size increases, which was demonstrated empirically to be true for both [OLS](ols/#consistency) and [WLS](wls/#consistency).
In this case we follow the same procedure whereby we manufacture 100,000 random samples from our data generating process and proceed to
fit both OLS and GLS models to each sample. In this case we repeat the process 5 times for sample sizes ranging from 20 to 100 inclusive
in steps of 20, and capture all estimates in a `DataFrame` before plotting the resulting frequency distributions of the parameter estimates.
The plots below clearly demonstrate the reduction in estimation variance as the sample size increases, which confirms the consistency of
the estimator, at least empirically.

<?prettify?>
```java
final double beta = 4d;
final double rho = 0.5d;
final double alpha = 20d;
final double sigma = 10d;
final int regressionCount = 100000;
final Range<Integer> sampleSizes = Range.of(20, 120, 20);
final Range<Integer> rows = Range.of(0, regressionCount);
final DataFrame<Integer,String> results = DataFrame.of(rows, String.class, columns -> {
    sampleSizes.forEach(n -> {
        columns.add(String.format("Beta(n=%s)", n), Double.class);
        columns.add(String.format("Alpha(n=%s)", n), Double.class);
    });
});

sampleSizes.forEach(n -> {
    System.out.println("Running " + regressionCount + " regressions for n=" + n);
    final String betaKey = String.format("Beta(n=%s)", n);
    final String alphaKey = String.format("Alpha(n=%s)", n);
    results.rows().parallel().forEach(row -> {
        DataFrame<Integer,String> data = sample(alpha, beta, rho, sigma, n, false);
        DataFrame<Integer,Integer> omega = createOmega(n, rho);
        data.regress().gls("Y", "X", omega, true, model -> {
            final double alphaHat = model.getInterceptValue(Field.PARAMETER);
            final double betaHat = model.getBetaValue("X", Field.PARAMETER);
            row.setDouble(alphaKey, alphaHat);
            row.setDouble(betaKey, betaHat);
            return Optional.empty();
        });
    });
});

Array.of("Beta", "Alpha").forEach(coeff -> {
    final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
    Chart.create().withHistPlot(coeffResults, 250, true, chart -> {
        chart.plot().axes().domain().label().withText("Coefficient Estimate");
        chart.title().withText(coeff + " Histograms of " + regressionCount + " Regressions");
        chart.subtitle().withText(coeff + " Variance decreases as sample size increases");
        chart.legend().on().bottom();
        chart.show(700, 400);
    });
});
```

<p align="center">
    <img class="chart" src="../../images/gls/gls-beta-consistency.png"/>
    <img class="chart" src="../../images/gls/gls-alpha-consistency.png"/>
</p>

The bar charts below illustrate the monotonically decreasing variance in both the \\(\beta\\) and \\(\alpha\\) coefficients.

<div style="float:left;width:50%;">
    <img class="chart" src="../../images/gls/gls-beta-variance.png"/>
</div>
<div style="float:left;width:50%;">
    <img class="chart" src="../../images/gls/gls-alpha-variance.png"/>
</div>

The code to generate this plot is as follows:

<?prettify?>
```java
Array<DataFrame<String,StatType>> variances = Array.of("Beta", "Alpha").map(value -> {
    final String coefficient = value.getValue();
    final Matcher matcher = Pattern.compile(coefficient + "\\(n=(\\d+)\\)").matcher("");
    return results.cols().select(column -> {
        final String name = column.key();
        return matcher.reset(name).matches();
    }).cols().mapKeys(column -> {
        final String name = column.key();
        if (matcher.reset(name).matches()) return matcher.group(1);
        throw new IllegalArgumentException("Unexpected column name: " + column.key());
    }).cols().stats().variance().transpose();
});

Chart.show(2, Array.of(
    Chart.create().withBarPlot(variances.getValue(0), false, chart -> {
        chart.title().withText("Beta variance with sample size");
        chart.plot().style(StatType.VARIANCE).withColor(new Color(255, 100, 100));
        chart.plot().axes().range(0).label().withText("Beta Variance");
        chart.plot().axes().domain().label().withText("Sample Size");
    }),
    Chart.create().withBarPlot(variances.getValue(1), false, chart -> {
        chart.title().withText("Beta variance with sample size");
        chart.plot().style(StatType.VARIANCE).withColor(new Color(102, 204, 255));
        chart.plot().axes().range(0).label().withText("Alpha Variance");
        chart.plot().axes().domain().label().withText("Sample Size");
    }))
);
```
