## Weighted Least Squares (WLS)

### Introduction

When fitting an OLS regression model, it may become apparent that there is an inconsistent variance in the residuals, which is known as 
[heteroscedasticity](https://en.wikipedia.org/wiki/Heteroscedasticity). As discussed earlier, this is a violation of one of the 
[Gauss Markov](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) assumptions, and therefore OLS is no longer the Best Linear Unbiased 
Estimator (BLUE). An OLS model in such circumstances is still expected to be [unbiased](https://en.wikipedia.org/wiki/Bias_of_an_estimator) 
and [consistent](https://en.wikipedia.org/wiki/Consistent_estimator), however it will not be the most [efficient](https://en.wikipedia.org/wiki/Efficiency_(statistics)) estimator which suggests there are other estimators 
that exhibit lower variance in parameter estimates. More concerning is that OLS is likely to yield biased estimates of the standard errors of the 
coefficients, making statistical interference unreliable.

### Theory

To address the issue of heteroscedasticity, a Weighted Least Squares (WLS) regression model is used in favour of OLS, which in effect applies 
a transformation to the original model so that the transformed model does in fact exhibit [homoscedastic](https://en.wikipedia.org/wiki/Homoscedasticity) 
errors. To demonstrate, consider the usual form of the regression model, but in this case we assume the variance in the error term follows a 
form of \\( Var[\epsilon] = \sigma^2 D \\) where D is a diagonal matrix of factors that scales the variance appropriately.

$$ Y = X \beta + \epsilon \ \ \ \ where \ E[\epsilon]=0 \ \ \ \ Var[\epsilon] = \sigma^2 D  $$

Now let us assume there exists a matrix P that can transform the above model such that the resulting error term becomes homoscedastic, 
and therefore satisfies the [Gauss Markov](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) assumption of constant variance.

$$ P Y = P X \beta + P \epsilon \ \ \ \ where \ E[P\epsilon]=0 \ \ \ \ Var(P \epsilon) = \sigma^2 I $$

Since the [Gauss Markov](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) assumptions are satisfied for the transformed system, 
we can apply OLS in the usual fashion. The next logical question therefore is, how do we come up with P? It turns out that we can solve for P 
in terms of D, the latter of which we can estimate reasonably well from the sample data. Consider the following derivation, where the key
assumption is that P is a symmetric matrix and therefore \\( P^T = P \\).

$$ \begin{align}
Var(P \epsilon) & = \sigma^2 I \\\\
P Var(\epsilon) P^T & = \sigma^2 I \\\\
\sigma^2 P D P^T & = \sigma^2 I \\\\
P D P^ T & = I \\\\
D P^T & = P^{-1} \\\\
D & = (P^2)^{-1} \\\\
P & = D^{-\frac{1}{2}}
\end{align} $$
 
Now let us re-write the transformed model in terms of Z, B and E as follows:

$$ Z = B \beta + E \ \ \ \  where \ Z = P Y, \ \ B = PX, \ \ and \\ E = P \epsilon $$
 
We can use the standard OLS estimator for this since we can assume it satisifies the [Gauss Markov](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) assumptions.

$$ \hat{\beta}_{ols} = (B^T B)^{-1} B^T Z $$

Knowing that \\(B=PX\\) and that \\( P = D^{-\frac{1}{2}} \\)we can write the WLS estimator as follows:

$$ \hat{\beta}_{wls} = (X^T P^T P X)^{-1} X^T P^T P Y  $$

$$ \hat{\beta}_{wls} = (X^T D^{-1} X)^{-1} X^T D^{-1} Y $$

We know that D is a diagonal matrix, so the inverse is also a diagonal matrix where the elements are simply the reciprocal of the non-zero 
elements of D, which we can think of as weights. This makes intuitive sense as the higher the variance, the lower the weight applied to those 
observations. We can therefore declare that the diagonal weight matrix \\( W = D^{-1} \\) and can proceed to write the estimator as follows: 

 $$ \hat{\beta}_{wls} = (X^T W X)^{-1} X^T W Y $$

### Example

In [Regression Analysis By Example](http://stats.idre.ucla.edu/spss/examples/chp/regression-analysis-by-example-third-editionchatterjee-hadi-and-pricedata-files/) 
by Chatterjee, Hadi & Price, one of the demonstrations involves a study of 27 industrial companies of various sizes, where the number of workers and 
number of supervisors was recorded. The study attempts to fit a linear model to this data to assess the nature of the relationship between these 
two types of employee. As it turns out, when you plot the data using a scatter plot, it becomes apparent that the variance in the dependent variable 
(number of supervisors) gets larger as the independent variable (number of workers) gets larger. The Morpheus code below can be used to load and plot 
this data as follows:

<?prettify?>
```java
DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
Chart.of(frame, "x",Double.class, chart -> {
    chart.plot(0).withPoints();
    chart.style("y").withPointsVisible(true).withColor(Color.BLUE).withPointShape(ChartShape.DIAMOND);
    chart.title().withText("Supervisors (y) vs Workers (x)");
    chart.subtitle().withText("Source: Regression by Example, Chatterjee & Price (1977)");
    chart.axes().domain().label().withText("Worker Count");
    chart.axes().range(0).label().withText("Supervisor Count");
    chart.trendLine().add("y", "OLS");
    chart.show();
});
```

<p align="center">
    <img src="../../images/wls/wls-chatterjee1.png"/>
</p>

If the increasing variance in the dependent variable is not obvious from the scatter plot, it can be useful to plot the residuals from an OLS
regression against the fitted values. The code below shows how to do this, and is followed by the resulting plot.

<?prettify?>
```java
DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
frame.regress().ols("y", "x", model -> {
    Chart.residualsVsFitted(model, chart -> {
        chart.title().withText("OLS Residuals vs Fitted Y Values");
        chart.axes().domain().label().withText("Y(fitted)");
        chart.axes().range(0).label().withText("OLS Residual");
        chart.legend().off();
        chart.show();
    });
    return Optional.empty();
});
```

<p align="center">
    <img src="../../images/wls/wls-chatterjee2.png"/>
</p>

While it is pretty clear from the above plots that heteroscedasticity is present in the data, which is a violation of one of the
Gauss Markov assumptions, let us first proceed by running an OLS regression in the usual fashion. The OLS estimates will be useful for 
comparison with those generated by a WLS regression, but more importantly, we can use the residuals of the former to estimate the weight 
matrix required by the latter. The code below runs the OLS regression and the prints the results to standard out.

<?prettify?>
```java
DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
frame.regress().ols("y", "x", model -> {
    System.out.println(model);
    return Optional.empty();
});
```

<div class="frame"><pre class="frame">
===========================================================================================
                                 Linear Regression Results                                                           
===========================================================================================
Model:                                 OLS    R-Squared:                          0.7759
Observations:                           27    R-Squared(adjusted):                0.7669
DF Model:                                1    F-Statistic:                       86.5435
DF Residuals:                           25    F-Statistic(Prob):                 1.35E-9
Standard Error:                    21.7293    Runtime(millis)                         81
Durbin-Watson:                      2.6325                                              
===========================================================================================
   Index    |  PARAMETER  |  STD_ERROR  |  T_STAT  |  P_VALUE   |  CI_LOWER  |  CI_UPPER  |
-------------------------------------------------------------------------------------------
 Intercept  |    14.4481  |      9.562  |   1.511  |  1.433E-1  |   -5.2453  |   34.1414  |
         x  |     0.1054  |     0.0113  |  9.3029  |   1.35E-9  |     0.082  |    0.1287  |
===========================================================================================
</pre></div>

The next step is to use the residuals from the OLS model in order to estimate the weight matrix for WLS. There is no hard rule on how to do 
this, and each case will require the researcher to come up with an approach for estimating W that makes sense given the sample dataset under
investigation. In this particular case however, it is clear that the variance in the dependent variable is proportional to the level of the 
independent variable, so we could fit a linear model to the OLS residuals against the independent variable and use that as a proxy for the
change in variance. Obviously the variance cannot be negative, so we could regress the absolute value of the residuals or the residuals squared.
The code below generates a plot of the absolute value of the residuals against the predictor, and fits and OLS model to the dataset.

<?prettify?>
```java
DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
frame.regress().ols("y", "x", model -> {
    final DataFrame<Integer,String> residuals = model.getResiduals();
    final DataFrame<Integer,String> residualsAbs = residuals.mapToDoubles(v -> Math.abs(v.getDouble()));
    final DataFrame<Integer,String> xValues = frame.cols().select("x");
    final DataFrame<Integer,String> newData = DataFrame.concatColumns(residualsAbs, xValues);
    Chart.of(newData, "x", Double.class, chart -> {
        chart.plot(0).withPoints();
        chart.style("Residuals").withPointsVisible(true).withColor(Color.BLUE).withPointShape(ChartShape.DIAMOND);
        chart.title().withText("ABS(Residuals) vs Predictor of Original Regression");
        chart.subtitle().withText("Regression line is a proxy for change in variance of dependent variable");
        chart.axes().domain().label().withText("Worker Count");
        chart.axes().range(0).label().withText("|Residual|");
        chart.trendLine().add("Residuals", "Trend");
        chart.show();
    });
    return Optional.empty();
});
```

<p align="center">
    <img src="../../images/wls/wls-chatterjee4.png"/>
</p>

We can now use the reciprocal of the fitted values of this regression as the elements of the diagonal weight matrix, W. The code
below generates this array of weights which can then be passed to the `DataFrame.regress().wls()` method.

<?prettify?>
```java
/**
 * Returns the vector of weights for the WLS regression by regressing |residuals| on the predictor
 * @param frame     the frame of original data
 * @return          the weight vector for diagonal matrix in WLS
 */
private Array<Double> computeWeights(DataFrame<Integer,String> frame) {
    return frame.regress().ols("y", "x", model -> {
        final DataFrame<Integer,String> residuals = model.getResiduals();
        final DataFrame<Integer,String> residualsAbs = residuals.mapToDoubles(v -> Math.abs(v.getDouble()));
        final DataFrame<Integer,String> xValues = frame.cols().select("x");
        final DataFrame<Integer,String> newData = DataFrame.concatColumns(residualsAbs, xValues);
        return newData.cols().ols("Residuals", "x", ols -> {
            ols.withIntercept(false);
            final DataFrame<Integer,String> yHat = ols.getFittedValues();
            final double[] weights = yHat.colAt(0).toDoubleStream().map(v -> 1d / Math.pow(v, 2d)).toArray();
            return Optional.of(Array.of(weights));
        });
    }).orElse(null);
}
```

Now that we have the diagonal elements of the W matrix that describes how much weight we wish to apply to each observation of our 
original dataset, we can run a Weighted Least Squares regression. The code below does exactly this, and prints the model summary
results to standard out.

<?prettify?>
```java
DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
Array<Double> weights = computeWeights(frame);
frame.regress().wls("y", "x", weights, model -> {
    System.out.println(model);
    return Optional.empty();
});
```

<div class="frame"><pre class="frame">
=============================================================================================
                                  Linear Regression Results                                                            
=============================================================================================
Model:                                   WLS    R-Squared:                            0.8785
Observations:                             27    R-Squared(adjusted):                  0.8737
DF Model:                                  1    F-Statistic:                        180.7789
DF Residuals:                             25    F-Statistic(Prob):                 6.044E-13
Standard Error:                       0.0227    Runtime(millis)                           99
Durbin-Watson:                        2.4675                                                
=============================================================================================
   Index    |  PARAMETER  |  STD_ERROR  |  T_STAT   |   P_VALUE   |  CI_LOWER  |  CI_UPPER  |
---------------------------------------------------------------------------------------------
 Intercept  |     3.8033  |     4.5697  |   0.8323  |   4.131E-1  |   -5.6083  |   13.2149  |
         x  |      0.121  |      0.009  |  13.4454  |  6.044E-13  |    0.1025  |    0.1395  |
=============================================================================================
</pre></div>

There are a few notable differences in the output between the OLS and WLS models as follows:

 1. The WLS model yields lower parameter standard errors than OLS. 
 2. The WLS model has narrower confidence intervals for the parameters than the OLS.
 3. The WLS model has a higher \\(R^2\\) than OLS
 
Finally, the code below generates the scatter plot of the data while adding a regression line for both the OLS (red line) and WLS (green line) 
estimates to see how they compare. It is clear that the WLS regression line is being pulled closer to observations associated with lower values 
of the independent variable compared to the OLS line, which equally weights all observations.
 
<?prettify?>
```java
/**
 * Generate a scatter plot with both OLS and WLS regression lines
 */
public void plotCompare() throws Exception {

    DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
    double[] x = frame.colAt("x").toDoubleStream().toArray();

    DataFrameLeastSquares<Integer,String> ols = frame.cols().ols("y", "x", Optional::of).orElse(null);
    double olsAlpha = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
    double olsBeta = ols.getBetaValue("x", DataFrameLeastSquares.Field.PARAMETER);
    DataFrame<Integer,String> olsFit = createFitted(olsAlpha, olsBeta, x, "OLS");

    Array<Double> weights = computeWeights(frame);
    DataFrameLeastSquares<Integer,String> wls = frame.cols().wls("y", "x", weights, Optional::of).orElse(null);
    double wlsAlpha = wls.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
    double wlsBeta = wls.getBetaValue("x", DataFrameLeastSquares.Field.PARAMETER);
    DataFrame<Integer,String> wlsFit = createFitted(wlsAlpha, wlsBeta, x, "WLS");

    Chart.withPoints(frame, "x", Double.class, chart -> {
        chart.plot(0).withPoints();
        chart.style("y").withColor(Color.BLUE).withPointsVisible(true);
        chart.data().add(olsFit, "x");
        chart.data().add(wlsFit, "x");
        chart.plot(1).withLines();
        chart.plot(2).withLines();
        chart.title().withText("OLS vs WLS Regression Comparison");
        chart.subtitle().withText(String.format("Beta OLS: %.3f, Beta WLS: %.3f", olsBeta, wlsBeta));
        chart.axes().domain().label().withText("X-value");
        chart.axes().range(0).label().withText("Y-value");
        chart.style("OLS").withColor(Color.RED).withLineWidth(2f);
        chart.style("WLS").withColor(Color.GREEN).withLineWidth(2f);
        chart.legend().on().right();
        chart.show();
    });
}


/**
 * Returns a DataFrame of fitted values given alpha, beta and the x-values
 * @param alpha     the alpha or intercept parameter
 * @param beta      the beta or slope parameter
 * @param x         the x-values or independent variable values
 * @param yName     the name for the fitted values
 * @return          the newly created DataFrame
 */
private DataFrame<Integer,String> createFitted(double alpha, double beta, double[] x, String yName) {
    return DataFrame.ofDoubles(Range.of(0, x.length), Array.of("x", yName), v -> {
        switch (v.colOrdinal()) {
            case 0:  return x[v.rowOrdinal()];
            default: return alpha + beta * x[v.rowOrdinal()];
        }
    });
}
```

<p align="center">
    <img src="../../images/wls/wls-chatterjee3.png"/>
</p>

### Unbiasedness

Conditional on the [Gauss Markov](ols/#gauss-markov-assumptions) assumptions being met for the transformed linear system as described earlier, 
parameter estimates are expected to be unbiased. Similar to the [OLS example](ols/#unbiasedness), we can demonstrate this empirically by creating 
many randomly generated 2-D data samples with the required characteristics given known population parameters for the slope and intercept. 

The function below returns a Morpheus `DataFrame` with two columns representing X and Y values, where the Y values are generated by a linear 
process with given parameters \\( \alpha \\) and \\( \beta \\). White noise is added to the Y values with increasing variance as X increases, 
which will yield a dataset with the desired characteristics (i.e. heteroscedasticity).

<?prettify?>
```java
/**
 * Returns a sample dataset based on a known population process using the linear coefficients provided
 * The sample dataset exhibits increasing variance proprtional to the independent variable.
 * @param alpha     the intercept term for population process
 * @param beta      the slope term for population process
 * @param startX    the start value for independent variable
 * @param stepX     the step size for independent variable
 * @return          the frame of XY values
 */
private DataFrame<Integer,String> sample(double alpha, double beta, double startX, double stepX, int n) {
    Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
    Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
        final double xValue = xValues.getDouble(v.index());
        final double yFitted = alpha + beta * xValue;
        final double stdDev = xValue * 2d;
        return new NormalDistribution(yFitted, stdDev).sample();
    });
    Range<Integer> rowKeys = Range.of(0, n);
    return DataFrame.of(rowKeys, String.class, columns -> {
        columns.add("X", xValues);
        columns.add("Y", yValues);
    });
}
```

To get a sense of what this data looks like, the code below generates 4 random samples of the dataset with 100 observations and plots them
on a scatter chart with a fitted OLS regression line. It should be clear from these charts that the dispersion in the dependent variable is 
increasing for larger values of the independent variable.

<?prettify?>
```java
final double beta = 4d;
final double alpha = 20d;
Stream<Chart> charts = IntStream.range(0, 4).mapToObj(i -> {
    DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, 100);
    String title = "Sample %s Dataset, Beta: %.3f Alpha: %.3f";
    String subtitle = "Parameter estimates, Beta^: %.2f, Alpha^: %.2f";
    DataFrameLeastSquares<Integer,String> ols = frame.cols().ols("Y", "X", Optional::of).get();
    double betaHat = ols.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
    double alphaHat = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
    return Chart.of(frame, "X", Double.class, chart -> {
        chart.plot(0).withPoints();
        chart.title().withText(String.format(title, i, beta, alpha));
        chart.title().withFont(new Font("Arial", Font.BOLD, 14));
        chart.subtitle().withText(String.format(subtitle, betaHat, alphaHat));
        chart.style("Y").withColor(Color.RED).withPointsVisible(true);
        chart.trendLine().add("Y", "OLS");
    });
});
Chart[] chartArray = charts.toArray(Chart[]::new);
ChartEngine.getDefaultEngine().show(4, 4, chartArray);
```

<p align="center">
    <img src="../../images/wls/wls-samples.png"/>
</p>

Now that we have a function that can generate data samples given some population parameters and with the appropriate variance characteristics,
we can proceed to run regressions on these samples and record the estimates and assess whether they are indeed centered on the known 
population values. The code below executes 100,000 regressions, collects the sample estimates for each run in a `DataFrame`, and then
plots a histogram of the results.

<?prettify?>
```java
int n = 100;
double beta = 4d;
double alpha = 20d;
int regressionCount = 100000;
Range<Integer> rows = Range.of(0, regressionCount);
Array<String> columns = Array.of("Beta", "Alpha");
DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

results.rows().parallel().forEach(row -> {
    final DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, n);
    final Array<Double> weights = computeWeights(frame);
    frame.regress().wls("Y", "X", weights, true, model -> {
        final double alphaHat = model.getInterceptValue(Field.PARAMETER);
        final double betaHat = model.getBetaValue("X", Field.PARAMETER);
        row.setDouble("Alpha", alphaHat);
        row.setDouble("Beta", betaHat);
        return Optional.empty();
    });
});

Array.of("Beta", "Alpha").forEach(coeff -> {
    final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
    Chart.hist(coeffResults, 250, chart -> {
        String title = "%s Histogram of %s WLS regressions";
        String subtitle = "%s estimate unbiasedness, Actual: %.2f, Mean: %.2f, Variance: %.2f";
        double actual = coeff.equals("Beta") ? beta : alpha;
        double estimate = coeffResults.colAt(coeff).stats().mean();
        double variance = coeffResults.colAt(coeff).stats().variance();
        Color color = coeff.equals("Beta") ? new Color(255, 100, 100) : new Color(102, 204, 255);
        chart.style(coeff).withColor(color);
        chart.axes().domain().label().withText(coeff + " Estimate");
        chart.title().withText(String.format(title, coeff, regressionCount));
        chart.subtitle().withText(String.format(subtitle, coeff, actual, estimate, variance));
        chart.show(700, 400);
    });
});
```

Obviously the exact estimates for this will vary each time given the stochastic term in our model, however the plots below over a very large
number of regressions clearly demonstrate that the distribution of our estimates is centered on the known population value. In fact, for the
results printed below, the **mean** slope and intercept coefficient was an exact match to the known population value, at least to two 
decimal places.

<p align="center">
    <img src="../../images/wls/wls-Beta-unbiasedness.png"/>
    <img src="../../images/wls/wls-Alpha-unbiasedness.png"/>
</p>


### Efficiency

Earlier it was suggested that in the presence of [heteroscedasticity](https://en.wikipedia.org/wiki/Heteroscedasticity), Ordinary Least Squares 
(OLS) was still expected to be **unbiased** and **consistent** but less [**efficient**](https://en.wikipedia.org/wiki/Efficient_estimator) than 
Weighted Least Squares (WLS) in that the variance of parameter estimates are likely to be higher for OLS. This section attempts to show this 
empirically using the Morpheus library.

To demonstrate the relative efficiency of the two estimators, we can use the same data generating function introduced earlier, and simply run 
both OLS and WLS regressions on the datasets to see how they compare. In each case, we can capture the parameter estimates from each regression, 
and then plot the resulting frequency distributions.

The code below runs OLS and WLS regressions on 100,000 samples created by our data generation function, and plots the frequency 
distributions of the resulting \\(\beta\\) and \\(\alpha\\) estimates. These results should ideally allow us to conclude that WLS estimates 
have lower estimation variance than OLS, and we should also be able to confirm that OLS is still **unbiased** in the presence of 
heteroscedasticity, just not BLUE. The following code executes these regressions, and then generates the plots.

<?prettify?>
```java
final int n = 100;
final double beta = 4d;
final double alpha = 20d;
final int regressionCount = 100000;
Range<Integer> rows = Range.of(0, regressionCount);
Array<String> columns = Array.of("Beta(OLS)", "Alpha(OLS)", "Beta(WLS)", "Alpha(WLS)");
DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

results.rows().parallel().forEach(row -> {
    final DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, n);
    frame.regress().ols("Y", "X", true, model -> {
        double alphaHat = model.getInterceptValue(Field.PARAMETER);
        double betaHat = model.getBetaValue("X", Field.PARAMETER);
        row.setDouble("Alpha(OLS)", alphaHat);
        row.setDouble("Beta(OLS)", betaHat);
        return Optional.empty();
    });

    final Array<Double> weights = computeWeights(frame);
    frame.regress().wls("Y", "X", weights, true, model -> {
        double alphaHat = model.getInterceptValue(Field.PARAMETER);
        double betaHat = model.getBetaValue("X", Field.PARAMETER);
        row.setDouble("Alpha(WLS)", alphaHat);
        row.setDouble("Beta(WLS)", betaHat);
        return Optional.empty();
    });
});

Array.of("Alpha", "Beta").forEach(coeff -> {
    final String olsKey = coeff + "(OLS)";
    final String wlsKey = coeff + "(WLS)";
    final DataFrame<Integer,String> data = results.cols().select(olsKey, wlsKey);
    Chart.hist(data, 350, chart -> {
        double meanOls = results.colAt(olsKey).stats().mean();
        double stdOls = results.colAt(olsKey).stats().stdDev();
        double meanWls = results.colAt(wlsKey).stats().mean();
        double stdWls = results.colAt(wlsKey).stats().stdDev();
        double coeffAct = coeff.equals("Alpha") ? alpha : beta;
        String title = "%s Histogram from %s OLS & WLS Regressions (n=%s)";
        String subtitle = "Actual: %.4f, Mean(OLS): %.4f, Std(OLS): %.4f, Mean(WLS): %.4f, Std(WLS): %.4f";
        chart.title().withText(String.format(title, coeff, regressionCount, n));
        chart.title().withFont(new Font("Arial", Font.BOLD, 15));
        chart.subtitle().withText(String.format(subtitle, coeffAct, meanOls, stdOls, meanWls, stdWls));
        chart.axes().domain().label().withText(coeff + " Estimates");
        chart.legend().on().bottom();
        chart.show(700, 400);
    });
});
```
Inspection of the histograms below of the \\(\alpha \\) and \\(\beta \\) estimates over the 100,000 regressions in the simulation
clearly demonstrate that both OLS and WLS are **unbiased** in that they are centered on the known population values for each
coefficient. The efficiency of each model however is clearly different, and as expected, the WLS regression model yields much lower 
variance in both the intercept and slope estimates compared to OLS.

<p align="center">
    <img src="../../images/wls/wls-beta-histogram.png"/>
    <img src="../../images/wls/wls-alpha-histogram.png"/>
</p>

### Consistency

Having empirically demonstrated the [unbiasedness](https://en.wikipedia.org/wiki/Bias_of_an_estimator) and [efficiency](https://en.wikipedia.org/wiki/Efficient_estimator) 
of the WLS estimator, this section attempts to assess the [consistency](https://en.wikipedia.org/wiki/Consistent_estimator) using the same data 
generating function introduced earlier. A consistent estimator is one that has a property such that as the sample size of the data being fitted 
increases, the variance of the estimates around the *true* value decreases. In other words, the estimated coefficients [converge in probability](https://en.wikipedia.org/wiki/Convergence_of_random_variables#Convergence_in_probability) 
on the true population values.

The code below runs 100,000 regressions with a *true* beta of 4 and an intercept or alpha of 20, except in this case the process is repeated 5 times 
with sample sizes of 100, 200, 300, 400 and 500 (so 500,000 regressions in all). The slope and intercept coefficients for all these runs are captured 
in a `DataFrame`, which is then used to generate a histogram in order to be able to visualize the variance in the estimates for each scenario.

<?prettify?>
```java
final double beta = 4d;
final double alpha = 20d;
final int regressionCount = 100000;
final Range<Integer> sampleSizes = Range.of(100, 600, 100);
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
        final DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, n);
        final Array<Double> weights = computeWeights(frame);
        frame.regress().wls("Y", "X", weights, true, model -> {
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
    Chart.hist(coeffResults, 250, chart -> {
        chart.axes().domain().label().withText("Coefficient Estimate");
        chart.title().withText(coeff + " Histograms of " + regressionCount + " Regressions");
        chart.subtitle().withText(coeff + " Estimate distribution as sample size increases");
        chart.legend().on().bottom();
        chart.show(700, 400);
    });
});
```

The plots below demonstrate a clear pattern of decreasing variance in the slope estimate as the sample size increases. With
regard to the intercept estimate, the evidence is less obvious, and the chart suggests only a maringal reduction in variance.

<p align="center">
    <img src="../../images/wls/wls-Beta-consistency.png"/>
    <img src="../../images/wls/wls-Alpha-consistency.png"/>
</p>

To get a better sense of the trend in estimation variance as sample size increases, we can compute the variance for all estimates
in a given run, and generate a bar plot of the results. The visualization below suggests the variance in the intercept is decreasing,
just not as aggressively as the slope coefficient in this case.

<p align="center">
    <img src="../../images/wls/wls-consistency.png"/>
</p>

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

ChartEngine.getDefaultEngine().show(1, 2,
    Chart.of(variances.getValue(0), chart -> {
        chart.plot(0).withBars(0d);
        chart.style(StatType.VARIANCE).withColor(new Color(255, 100, 100));
        chart.title().withText("Beta variance with sample size");
        chart.axes().range(0).label().withText("Beta Variance");
        chart.axes().domain().label().withText("Sample Size");
    }),
    Chart.of(variances.getValue(1), chart -> {
        chart.plot(0).withBars(0d);
        chart.style(StatType.VARIANCE).withColor(new Color(102, 204, 255));
        chart.title().withText("Alpha variance with sample size");
        chart.axes().range(0).label().withText("Alpha Variance");
        chart.axes().domain().label().withText("Sample Size");
    })
);
```
