## Ordinary Least Squares (OLS)

### Introduction

[Regression analysis](https://en.wikipedia.org/wiki/Regression_analysis) is a statistical technique used to fit a model expressed in 
terms of one or more variables to some data. In particular, it allows one to analyze the relationship of a dependent variable (also 
referred to as the regressand) on one or more independent or predictor variables (also referred to as regressors), and assess how 
influential each of these are.  

There are many types of regression analysis techniques, however one of the most commonly used is based on fitting data to a linear model, 
and in particular using an approach called [Least Squares](https://en.wikipedia.org/wiki/Least_squares). The Morpheus API currently supports 
3 variations of Linear Least Squares regression, namely [Ordinary Least Squares](https://en.wikipedia.org/wiki/Ordinary_least_squares) (OLS), 
[Weighted Least Squares](https://en.wikipedia.org/wiki/Least_squares#Weighted_least_squares) (WLS) and [Generalized Least Squares](https://en.wikipedia.org/wiki/Generalized_least_squares)
(GLS). The following section reviews some OLS regression theory, and provides an example of how to use the Morhpeus API to apply this technique.

### Theory

A linear regression model in matrix form can be expressed as:

$$ Y = X \beta + \epsilon \ \ \ \ where \ E[\epsilon]=0 \ \ \ \ Var[\epsilon] = \sigma^2 I  $$

Y represents an `nx1` vector of regressands, and X represents an `nxp` design matrix, where `n` is the number of observations in the data, 
and `p` represents the number of parameters to estimate. If the model includes an intercept term, the first column in the design matrix is 
populated with 1's and therefore the first entry in the `px1` \\(\beta\\) vector would represent the intercept value. The `nx1` \\(\epsilon\\) 
vector represents the error or disturbance term, which is assumed to have a conditional mean of 0 and also to be free of any serial correlation 
(see section below on the [Gauss Markov](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) assumptions).

A least squares regression model estimates \\(\beta\\) so as to minimize the sum of the squared error, or \\(\epsilon^T\epsilon\\). We 
can use the model equation above to express \\(\epsilon^T\epsilon\\) in terms of Y and X\\(\beta\\), differentiate this by \\(\beta\\), 
set the result to zero since we wish to minimize the squared error, and solve for \\(\beta\\) as follows:  

$$ \begin{align}
\epsilon^T\epsilon &= (Y - X \beta)^T(Y - X \beta) \\\\
 &= Y^TY - \beta^TX^TY - Y^TX\beta + \beta^T X^T X \beta \\\\
 &= Y^TY - 2\beta^T X^T Y + \beta^T X^T X \beta
\end{align} $$

We now differentiate this expression with respect to \\(\beta\\) and set it to zero which yields the following:

$$ \frac{d\epsilon^T\epsilon}{d\beta} = -2X^T Y + 2X^T X \beta = 0 $$

This expression can be re-arranged to solve for \\(\beta\\) and yields the following equation: 

$$ \beta = (X^T X)^{-1} X^T Y $$ 

The value of \\(\beta\\) can only be **estimated** given some sample data drawn from a population or data generating process, the *true* value is 
unknown. We usually refer to the estimate as \\(\hat{\beta}\\) (beta "hat") in order to distinguish it from the true population value. In addition, 
when expressing the model in terms of \\(\hat{\beta}\\), the stochastic term is referred to as *residuals* rather than *errors*, and while they 
are conceptually related, they are [not the same thing](https://en.wikipedia.org/wiki/Errors_and_residuals). Errors represent the deviation of 
observations from the unknown population line, while residuals represent the deviations from the estimation line, a subtle difference and easily 
confused. In terms of \\(\hat{\beta}\\) and residuals **u**, the model is expressed in the usual way. 

$$ Y = X \hat{\beta} + u \ \ \ \ where \ E[u]=0 \ \ \ \ Var[u] = \sigma^2 I  $$

The residuals are of course still assumed to be consistent with the [Gauss Markov](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) 
assumptions, and so the estimator for \\(\hat{\beta}\\) is: 

$$ \hat{\beta} = (X^T X)^{-1} X^T Y $$ 

### Solving

We can solve for \\(\hat{\beta}\\) directly by calculating the right hand side of the above equation, which is what the Morpheus library does
by default. There are situations in which this can present numerical stability issues, in which case it may be preferable to solve for \\(\hat{\beta}\\) 
by factorizing the design matrix X using a [QR decomposition](https://en.wikipedia.org/wiki/QR_decomposition) where Q represents an orthogonal matrix 
such that \\(Q^T Q = I\\) and thus \\(Q^T = Q^{-1}\\), and where R is an upper triangular matrix. The QR decomposition approach is based on the 
following reasoning.

$$ \begin{align}
X^T Y & = X^T X \hat{\beta}  \\\\
\big( QR \big)^T Y & = \big( QR \big)^T \big( QR \big) \hat{\beta} \\\\
R^T Q^T Y & = R^T Q^T Q R \hat{\beta} \\\\
R^T Q^T Y & = R^T R \hat{\beta} \\\\
(R^T)^{-1} R^T Q^T Y & = \big( R^T \big)^{-1} R^T R \hat{\beta} \\\\ 
Q^T Y & = R \hat{\beta}
\end{align} $$

This can be solved efficiently by [backward substitution](https://en.wikipedia.org/wiki/Triangular_matrix#Forward_and_back_substitution) because the R 
matrix is upper [triangular](https://en.wikipedia.org/wiki/Triangular_matrix), and therefore no inversion of the design matrix is required. The Morpheus 
library also supports estimating \\(\hat{\beta}\\) using this technique, which can be configured on a case by case basis via the `withSolver()` method 
on the `DataFrameLeastSquares` interface. 

### Diagnostics
 
Just like any other statistical technique, regression analysis is susceptible to [sampling error](https://en.wikipedia.org/wiki/Sampling_error), and 
it is therefore common to compute the variance of the parameter estimates, as well as their [standard error](https://en.wikipedia.org/wiki/Standard_error), 
which can then be used for [inferential](https://en.wikipedia.org/wiki/Statistical_inference) purposes. In the context of a linear regression, the 
[null hypotheses](https://en.wikipedia.org/wiki/Null_hypothesis) \\(H_{0}\\), is generally that the model parameters are zero. The parameter standard 
errors can be used to calculate a [t-statistic](https://en.wikipedia.org/wiki/T-statistic) and corresponding [p-value](https://en.wikipedia.org/wiki/P-value) 
in order to decide if \\(H_{0}\\) can be rejected in favour of the alternative hypothesis, and thus assert that the estimates are statistically 
significantly different from zero.

The variance of \\(\hat{\beta}\\) with respect to the true population value can be expressed as follows:

$$ Var(\hat{\beta}) = E[(\\hat{\beta} - \beta)(\\hat{\beta} - \beta)^T] $$

We substitute our population model \\( Y = X \beta + \epsilon \\) in our sample estimator \\( \hat{\beta} = (X^T X)^{-1} X^T Y \\) as follows:  

$$ \begin{align}
\hat{\beta} &= (X^T X)^{-1} X^T ( X \beta + \epsilon ) \\\\
\hat{\beta} &= (X^T X)^{-1} X^T X \beta + (X^T X)^{-1} X^T \epsilon  \\\\
\hat{\beta} &= \beta + (X^T X)^{-1} X^T \epsilon  \\\\
\hat{\beta} - \beta &= (X^T X)^{-1} X^T \epsilon  \\\\
\end{align} $$
 
With the above expression for \\( \hat{\beta} - \beta \\) we can now solve for the variance of the OLS estimator as follows: 

$$ \begin{align}
Var(\hat{\beta}) &= E[(\\hat{\beta} - \beta)(\\hat{\beta} - \beta)^T] \\\\
 & = E[((X^T X)^{-1} X^T \epsilon) ((X^T X)^{-1} X^T \epsilon)^T] \\\\
 & = E[(X^T X)^{-1} X^T \epsilon \epsilon^T X (X^T X)^{-1} ]
\end{align} $$
 
Given that the design matrix X is non-stochastic and the \\( E[\epsilon \epsilon^T] = \sigma^2 I \\):
 
$$ \begin{align}
Var(\hat{\beta}) & = (X^T X)^{-1} X^T E[\epsilon \epsilon^T] X (X^T X)^{-1} \\\\
 & = (X^T X)^{-1} X^T (\sigma^2 I) X (X^T X)^{-1} \\\\
 & = \sigma^2 I (X^T X)^{-1} X^T X (X^T X)^{-1} \\\\
 & = \sigma^2 I (X^T X)^{-1} \\\\
 & = \sigma^2 (X^T X)^{-1}
\end{align} $$

Other regression diagnostics that are calculated include the [coefficient of determination](https://en.wikipedia.org/wiki/Coefficient_of_determination) or
\\(R^2\\) which is a number that indicates the proportion of variance in the dependent variable that is explained by the independent variables, and is
documented in the table below. The parameter variance estimates as calculated above are used to compute the standard errors and a corresponding
[t-statistic](https://en.wikipedia.org/wiki/T-statistic) which can be used for statistical inference.

| Quantity                          | Description                                                                                                                      |
|-----------------------------------|:---------------------------------------------------------------------------------------------------------------------------------|
| Residual Sum of Squares (RSS)     | $$ RSS = \sum_{i=1}^n \big(y_{i} - \hat{y_{i}} \big)^2 = \sum_{i=1}^n \epsilon_{i}^2 = \epsilon^T \epsilon $$                    |
| Total Sum of Squares (TSS)        | $$ TSS = \sum_{i=1}^{n} \big(y_{i} - \overline{y}\big)^2 \, \textrm{where} \; \overline{y} = \frac{1}{n} \sum_{i=1}^n y_{i} $$   |
| Explained Sum of Squares (ESS)    | $$ ESS = \sum_{i=1}^{n} \big(\hat{y_{i}} - \overline{y}\big)^2 \, \textrm{where} \; \overline{y} = \frac{1}{n} \sum_{i=1}^n y_{i} $$   |
| R-Squared                         | $$ R^2 = 1 - \frac{RSS}{TSS}  $$                                                                                                 |
| R-Squared (Adjusted)              | $$ R^2_{adj} = 1 - \frac{ RSS * \big( n - 1 \big) }{ TSS * \big( n - p \big)}  $$                                                |
| Regression Standard Error         | $$ SE = \sqrt{ \frac{RSS}{ n - p }}  $$                                                                                          |
| Parameter Variance                | $$ Var(\hat{\beta}) = SE^2( X^T X )^{-1}  $$                                                                                     |   
| Parameter Std Errors              | $$ SE(\hat{\beta_{i}}) = \sqrt{ Var(\hat{\beta_{i}})} $$                                                                         |
| Parameter T-statistics            | $$ T_{\beta_{i}} = \frac{\hat{ \beta_{i}}}{ SE( \hat{ \beta_{i} } ) }    $$                                                      |

### Gauss Markov Assumptions

The [Gauss Markov Theorem](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) states that Ordinary Least Squares is the Best Linear Unbiased 
and Efficient (BLUE) estimator of \\(\beta\\), conditional on a certain set of assumptions being met. In this context, "best" means that there are
no other unbiased estimators with a smaller sampling variance than OLS. Unbiased means that that the expectation of \\( \hat{\beta}\\) is equal to the
population \\(\beta\\), or otherwise stated \\( E[ \hat{\beta} ] = \beta \\). The assumptions that must hold for OLS to be BLUE are as follows:

| Assumptions  | Description                                                                                                                                       |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| Assumption 1 | The regression model is linear in parameters, and therefore well specified                                                                        |
| Assumption 2 | The regressors are linearly independent, and therefore do not exhibit perfect [multicollinearity](https://en.wikipedia.org/wiki/Multicollinearity)| 
| Assumption 3 | The errors in the regression have a conditional mean of zero                                                                                      |
| Assumption 4 | The errors are [homoscedastic](https://en.wikipedia.org/wiki/Homoscedasticity), which means they exhibit constant variance                        | 
| Assumption 5 | The errors are uncorrelated between observations                                                                                                  |
| Assumption 6 | The errors are normally distributed, and independent and identically distributed (iid)                                                            |

#### ** Linear in Parameters **

The first assumption regarding linearity suggests that the dependent variable is a linear function of the independent variables. This does not imply that there
is a linear relationship between the independent and dependent variables, it only states the the model is linear in parameters. For example, a model of the form
\\(y = \alpha + \beta x^2 \\) qualifies as being linear in parameters, while \\(y = \alpha + \beta^2 x \\) does not. If the functional form of a  model under
investigation is not linear in parameters, it can often be transformed so as to render it linear.

#### ** Linearly Independent **

The second assumption that there is no perfect [multicollinearity](https://en.wikipedia.org/wiki/Multicollinearity) between the regressors is important, as if
it exists, the OLS estimator cannot be calculated. Another way of expressing this condition is that one of the independent variables cannot be a function of any 
of the other independent variables, and therefore the design matrix X must be non-singular, and therefore have full rank.

#### ** Strict exogeneity **
 
The third assumption above states that the disturbance term averages out to zero for any given instance of X, which implies that no observations of the independent 
variables convey any information about the error. Mathematically this is stated as \\( E[ \epsilon | X ] = 0 \\). This assumption is violated if the independent
variables are [stochastic](https://en.wikipedia.org/wiki/Stochastic_process) in nature, which can arise as a result of [measurement error](https://en.wikipedia.org/wiki/Errors-in-variables_models), or if there is 
[endogeneity](https://en.wikipedia.org/wiki/Endogeneity_(econometrics)) in the model.

#### ** Spherical Errors **

The fourth and fifth assumptions relate to the [covariance matrix](https://en.wikipedia.org/wiki/Covariance_matrix) of the error term, and specifically states 
that \\(E[ \epsilon \epsilon^T | X] = \sigma^2 I \\). There are two key concepts embedded in this statement, the first is that the disturbance term has uniform 
variance of \\(\sigma^2\\) regardless of the values of the independent variables, and is referred to as homoscedasticity. In addition, the off diagonal terms of 
the covariance matrix are assumed to be zero, which suggests there is no serial correlation between errors. Either or both of these assumptions may not hold in 
the real world, in which case a WLS or GLS estimator may prove to be a better, unbiased linear estimator. 


### Example

The Morpheus API defines an interface called `DataFrameRegression` which exposes a number of methods that support different linear regression techniques, namely 
OLS, WLS and GLS. There are overloaded methods that take one or more regressors in order to conveniently support simple and multiple linear regression. 

The regression interface, which operates on the column data in a `DataFrame`, can be accessed by calling the `regress()` method on the frame. If  a regression in 
the row dimension is required, simply call `transpose()` on the frame before calling `regress()`.

To illustrate an example, consider the same motor vehicle dataset introduced earlier, which can be loaded with the code below. The first 10 rows of this `DataFrame` 
is also included for inspection, and in this exercise we are going to be interested in the **EngineSize** and **Horsepower** columns. 

<?prettify?>
```java
static DataFrame<Integer,String> loadCarDataset() {
    return DataFrame.read().csv(options -> {
        options.setResource("http://zavtech.com/data/samples/cars93.csv");
        options.setExcludeColumnIndexes(0);
    });
}
```

<div class="frame"><pre class="frame">
 Index  |  Manufacturer  |    Model     |   Type    |  Min.Price  |   Price   |  Max.Price  |  MPG.city  |  MPG.highway  |       AirBags        |  DriveTrain  |  Cylinders  |  EngineSize  |  Horsepower  |  RPM   |  Rev.per.mile  |  Man.trans.avail  |  Fuel.tank.capacity  |  Passengers  |  Length  |  Wheelbase  |  Width  |  Turn.circle  |  Rear.seat.room  |  Luggage.room  |  Weight  |  Origin   |        Make        |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |         Acura  |     Integra  |    Small  |    12.9000  |  15.9000  |    18.8000  |        25  |           31  |                None  |       Front  |          4  |      1.8000  |         140  |  6300  |          2890  |              Yes  |             13.2000  |           5  |     177  |        102  |     68  |           37  |            26.5  |            11  |    2705  |  non-USA  |     Acura Integra  |
     1  |         Acura  |      Legend  |  Midsize  |    29.2000  |  33.9000  |    38.7000  |        18  |           25  |  Driver & Passenger  |       Front  |          6  |      3.2000  |         200  |  5500  |          2335  |              Yes  |             18.0000  |           5  |     195  |        115  |     71  |           38  |              30  |            15  |    3560  |  non-USA  |      Acura Legend  |
     2  |          Audi  |          90  |  Compact  |    25.9000  |  29.1000  |    32.3000  |        20  |           26  |         Driver only  |       Front  |          6  |      2.8000  |         172  |  5500  |          2280  |              Yes  |             16.9000  |           5  |     180  |        102  |     67  |           37  |              28  |            14  |    3375  |  non-USA  |           Audi 90  |
     3  |          Audi  |         100  |  Midsize  |    30.8000  |  37.7000  |    44.6000  |        19  |           26  |  Driver & Passenger  |       Front  |          6  |      2.8000  |         172  |  5500  |          2535  |              Yes  |             21.1000  |           6  |     193  |        106  |     70  |           37  |              31  |            17  |    3405  |  non-USA  |          Audi 100  |
     4  |           BMW  |        535i  |  Midsize  |    23.7000  |  30.0000  |    36.2000  |        22  |           30  |         Driver only  |        Rear  |          4  |      3.5000  |         208  |  5700  |          2545  |              Yes  |             21.1000  |           4  |     186  |        109  |     69  |           39  |              27  |            13  |    3640  |  non-USA  |          BMW 535i  |
     5  |         Buick  |     Century  |  Midsize  |    14.2000  |  15.7000  |    17.3000  |        22  |           31  |         Driver only  |       Front  |          4  |      2.2000  |         110  |  5200  |          2565  |               No  |             16.4000  |           6  |     189  |        105  |     69  |           41  |              28  |            16  |    2880  |      USA  |     Buick Century  |
     6  |         Buick  |     LeSabre  |    Large  |    19.9000  |  20.8000  |    21.7000  |        19  |           28  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1570  |               No  |             18.0000  |           6  |     200  |        111  |     74  |           42  |            30.5  |            17  |    3470  |      USA  |     Buick LeSabre  |
     7  |         Buick  |  Roadmaster  |    Large  |    22.6000  |  23.7000  |    24.9000  |        16  |           25  |         Driver only  |        Rear  |          6  |      5.7000  |         180  |  4000  |          1320  |               No  |             23.0000  |           6  |     216  |        116  |     78  |           45  |            30.5  |            21  |    4105  |      USA  |  Buick Roadmaster  |
     8  |         Buick  |     Riviera  |  Midsize  |    26.3000  |  26.3000  |    26.3000  |        19  |           27  |         Driver only  |       Front  |          6  |      3.8000  |         170  |  4800  |          1690  |               No  |             18.8000  |           5  |     198  |        108  |     73  |           41  |            26.5  |            14  |    3495  |      USA  |     Buick Riviera  |
</pre></div>

Based on our understanding of the factors that influence the power of an internal combustion engine, one might hypothesize that there is a positive and linear 
relationship between the size of an engine and how much horsepower it produces. . We can use the car dataset to test this hypothesis. First we can use the Morpheus 
library to generate a scatter plot of the data, with **EngineSize** on the x-axis and **Horsepower** on the y-axis as follows:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadCarDataset();
final String y = "Horsepower";
final String x = "EngineSize";
DataFrame<Integer,String> xy = frame.cols().select(y, x);
Chart.of(xy, x, Double.class, chart -> {
    chart.plot(0).withPoints();
    chart.style(y).withColor(Color.RED);
    chart.style(y).withPointsVisible(true).withPointShape(ChartShape.DIAMOND);
    chart.title().withText(y + " vs " + x);
    chart.axes().domain().label().withText(x);
    chart.axes().domain().format().withPattern("0.00;-0.00");
    chart.axes().range(0).label().withText(y);
    chart.axes().range(0).format().withPattern("0;-0");
    chart.show(845, 450);
});
```

<p align="center">
    <img src="../../images/ols/data-frame-ols1.png"/>
</p>

The scatter plot certainly appears to suggest that there is a positive relationship between **EngineSize** and **Horsepower**. In addition, it seems somewhat 
linear, however the dispersion appears to get more significant for larger engine sizes, which would be a violation of one of the [Gauss Markov](https://en.wikipedia.org/wiki/Gauss%E2%80%93Markov_theorem) 
assumptions, namely of [homoscedastic](https://en.wikipedia.org/wiki/Homoscedasticity) errors. Nevertheless, let us proceed to regress **Horsepower** on 
**EngineSize** and see what the results look like. The code below runs a single variable regression and simply prints the model results to standard-out 
for inspection.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadCarDataset();
String regressand = "Horsepower";
String regressor = "EngineSize";
frame.regress().ols(regressand, regressor, true, model -> {
    System.out.println(model);
    return Optional.empty();
});
```

<div class="frame"><pre class="frame">
==============================================================================================
                                   Linear Regression Results                                                            
==============================================================================================
Model:                                   OLS    R-Squared:                            0.5360
Observations:                             93    R-Squared(adjusted):                  0.5309
DF Model:                                  1    F-Statistic:                        105.1204
DF Residuals:                             91    F-Statistic(Prob):                  1.11E-16
Standard Error:                      35.8717    Runtime(millis)                           48
Durbin-Watson:                        1.9591                                                
==============================================================================================
   Index     |  PARAMETER  |  STD_ERROR  |  T_STAT   |   P_VALUE   |  CI_LOWER  |  CI_UPPER  |
----------------------------------------------------------------------------------------------
  Intercept  |    45.2195  |    10.3119  |   4.3852  |   3.107E-5  |    24.736  |   65.7029  |
 EngineSize  |    36.9633  |     3.6052  |  10.2528  |  7.573E-17  |    29.802  |   44.1245  |
==============================================================================================
</pre></div>

The regression results yield a slope coefficient of **36.96**, suggesting that for every additional litre of engine capacity, we can expect to add another
36.96 horsepower. While the [p-value](https://en.wikipedia.org/wiki/P-value) associated with the slope coefficient suggests that it is statistically 
significantly different from zero, it does not tell us about how relevant the parameter is in the regression. In this case we can reasonably surmise 
that engine size is relevant given our understanding of how an internal combustion engine works and what factors affect output power. Having said that, 
the coefficient of determination is perhaps lower than one might expect, and the heteroscedasticity of the residuals provides some hint that the model 
may be incomplete. In particular, [omitted-variable bias](https://en.wikipedia.org/wiki/Omitted-variable_bias) may be at play here, in the sense that 
there are other important factors that influence an engine's horsepower that we have not included.

While the code example above simply prints the model results to standard out, the illustration below demonstrates how to access all the relevant model 
outputs via the API. The `ols()` method takes a lambda parameter that consumes the regression model, which is an instance of the `DataFrameLeastSquares` 
interface, and provides all the relevant hooks to access the model inputs and outputs.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadCarDataset();
final String regressand = "Horsepower";
final String regressor = "EngineSize";
frame.regress().ols(regressand, regressor, true, model -> {
    assert (model.getRegressand().equals(regressand));
    assert (model.getRegressors().size() == 1);
    assertEquals(model.getRSquared(), 0.5359992996664269, 0.00001);
    assertEquals(model.getRSquaredAdj(), 0.5309003908715525, 0.000001);
    assertEquals(model.getStdError(), 35.87167658782274, 0.00001);
    assertEquals(model.getFValue(), 105.120393642, 0.00001);
    assertEquals(model.getFValueProbability(), 0, 0.00001);
    assertEquals(model.getBetaValue("EngineSize", Field.PARAMETER), 36.96327914, 0.0000001);
    assertEquals(model.getBetaValue("EngineSize", Field.STD_ERROR), 3.60518041, 0.0000001);
    assertEquals(model.getBetaValue("EngineSize", Field.T_STAT), 10.25282369, 0.0000001);
    assertEquals(model.getBetaValue("EngineSize", Field.P_VALUE), 0.0000, 0.0000001);
    assertEquals(model.getBetaValue("EngineSize", Field.CI_LOWER), 29.80203113, 0.0000001);
    assertEquals(model.getBetaValue("EngineSize", Field.CI_UPPER), 44.12452714, 0.0000001);
    assertEquals(model.getInterceptValue(Field.PARAMETER), 45.21946716, 0.0000001);
    assertEquals(model.getInterceptValue(Field.STD_ERROR), 10.31194906, 0.0000001);
    assertEquals(model.getInterceptValue(Field.T_STAT), 4.3851523, 0.0000001);
    assertEquals(model.getInterceptValue(Field.P_VALUE), 0.00003107, 0.0000001);
    assertEquals(model.getInterceptValue(Field.CI_LOWER), 24.73604714, 0.0000001);
    assertEquals(model.getInterceptValue(Field.CI_UPPER), 65.70288719, 0.0000001);
    System.out.println(model);
    return Optional.of(model);
});
```

Finally, the chart below adds the OLS trendline to the initial scatter plot to get a better sense of how the solution fits the data. 

<img src="../../images/ols/data-frame-ols2.png"/>

The code to generate this chart is as follows:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadCarDataset();
final String regressand = "Horsepower";
final String regressor = "EngineSize";
DataFrame<Integer,String> xy = frame.cols().select(regressand, regressor);
Chart.of(xy, regressor, Double.class, chart -> {
    chart.plot(0).withPoints();
    chart.style(regressand).withColor(Color.RED).withPointsVisible(true).withPointShape(ChartShape.DIAMOND);
    chart.trendLine().add(regressand, regressand + " (trend)").withColor(Color.BLACK);
    chart.title().withText(regressand + " regressed on " + regressor);
    chart.subtitle().withText("Single Variable Linear Regression");
    chart.title().withFont(new Font("Verdana", Font.BOLD, 16));
    chart.axes().domain().label().withText(regressor);
    chart.axes().domain().format().withPattern("0.00;-0.00");
    chart.axes().range(0).label().withText(regressand);
    chart.axes().range(0).format().withPattern("0;-0");
    chart.show();
});
```

### Unbiasedness

An Ordinary Least Squares estimator is said to be [unbiased](https://en.wikipedia.org/wiki/Bias_of_an_estimator) in the sense that 
if you run regressions on many samples of data generated from the same population process, the coefficient estimates from all these samples 
would be centered on the true population values. To demonstrate this empirically, we can define a population process in 2D space with a known 
slope and intercept coefficient, and then proceed to generate many samples from this process while adding Gaussian noise to the dependent variable 
in order to simulate the error term. The code below defines a data generating function that returns a `DataFrame` of X and Y values initialized 
from the population coefficients, while adding white noise scaled according to the standard deviation specified by the `sigma` parameter.

<?prettify?>
```java
/**
 * Returns a 2D sample dataset based on a population process using the coefficients provided
 * @param alpha     the intercept term for population process
 * @param beta      the slope term for population process
 * @param startX    the start value for independent variable
 * @param stepX     the step size for independent variable
 * @param sigma     the variance to add noise to dependent variable
 * @param n         the size of the sample to generate
 * @return          the frame of XY values
 */
DataFrame<Integer,String> sample(double alpha, double beta, double startX, double stepX, double sigma, int n) {
    final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
    final Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
        final double yfit = alpha + beta * xValues.getDouble(v.index());
        return new NormalDistribution(yfit, sigma).sample();
    });
    final Array<Integer> rowKeys = Range.of(0, n).toArray();
    return DataFrame.of(rowKeys, String.class, columns -> {
        columns.add("X", xValues);
        columns.add("Y", yValues);
    });
}
```

To get a sense of the nature of the dataset generated by this function for some chosen set of parameters, we can plot a number 
of samples. The code below plots 4 random samples of this population process with beta = 1.45, alpha = 4.15 and a sigma value of 20.

<?prettify?>
```java
final double beta = 1.45d;
final double alpha = 4.15d;
final double sigma = 20d;
Stream<Chart> charts = IntStream.range(0, 4).mapToObj(i -> {
    DataFrame<Integer,String> frame = sample(alpha, beta, 0, 1, sigma, 100);
    String title = "Sample %s Dataset, Beta: %.2f Alpha: %.2f";
    String subtitle = "Parameter estimates, Beta^: %.3f, Alpha^: %.3f";
    DataFrameLeastSquares<Integer,String> ols = frame.regress().ols("Y", "X", true, Optional::of).get();
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
    <img src="../../images/ols/ols-samples.png"/>
</p>

Given this data generating function, we can produce many samples from a known population process and then proceed to run OLS regressions on 
these samples. For each run we capture the coefficient estimates, and then plot a histogram of all the recorded estimates to confirm that the 
coefficients are indeed centered on the known population values. The following code performs this procedure for 100,000 regressions, and is
followed by the resulting plots.

<?prettify?>
```java
final int n = 100;
final double actAlpha = 4.15d;
final double actBeta = 1.45d;
final double sigma = 20d;
final int regressionCount = 100000;
final Range<Integer> rows = Range.of(0, regressionCount);
final Array<String> columns = Array.of("Beta", "Alpha");
final DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

//Run 100K regressions in parallel
results.rows().parallel().forEach(row -> {
    final DataFrame<Integer,String> frame = dataset(actAlpha, actBeta, 0, 1, sigma, n);
    frame.regress().ols("Y", "X", true, model -> {
        final double alpha = model.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
        final double beta = model.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
        row.setDouble("Alpha", alpha);
        row.setDouble("Beta", beta);
        return Optional.empty();
    });
});

Array.of("Beta", "Alpha").forEach(coefficient -> {
    Chart.hist(results, coefficient, 250, chart -> {
        final double mean = results.colAt(coefficient).stats().mean();
        final double stdDev = results.colAt(coefficient).stats().stdDev();
        final double actual = coefficient.equals("Beta") ? actBeta : actAlpha;
        final String title = "%s Histogram from %s Regressions (n=%s)";
        final String subtitle = "Actual: %.4f, Mean: %.4f, StdDev: %.4f";
        chart.title().withText(String.format(title, coefficient, regressionCount, n));
        chart.subtitle().withText(String.format(subtitle, actual, mean, stdDev));
        chart.show(700, 400);
    });
});
```

<p align="center">
    <img src="../../images/ols/ols-Alpha-unbiased.png"/>
    <img src="../../images/ols/ols-Beta-unbiased.png"/>
</p>

The alpha and beta histogram plots above clearly show that the distribution of the 100000 estimates of each coefficient are centered on the 
known population values. In the case of the slope coefficient, the known population value is 1.45 and the mean value over the 100000 estimates
is a good match. Similarly, the intercept estimate mean 4.1266 is very close to the known population value of 4.15.

### Consistency

An OLS estimator is said to be [consistent](https://en.wikipedia.org/wiki/Consistent_estimator) in the sense that as the sample size increases, 
the variance in the coefficient estimates should decrease. This can be demonstrated empirically once again using the data generation function introduced 
earlier. In this experiment we run a certain number of regressions based on samples generated from a known population process, but we would need to do 
this multiple times with increasing sample sizes. The code below implements this by running 100,000 regressions for sample sizes ranging from 100 to 500 
in steps of 100, and captures the coefficient estimates for each run. It then plots histograms for the beta and intercept estimates to illustrate the 
narrowing variance as sample size increases.

<?prettify?>
```java
final double actAlpha = 4.15d;
final double actBeta = 1.45d;
final double sigma = 20d;
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
        final DataFrame<Integer,String> frame = dataset(actAlpha, actBeta, 0, 1, sigma, n);
        frame.regress().ols("Y", "X", true, model -> {
            final double alpha = model.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
            final double beta = model.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
            row.setDouble(alphaKey, alpha);
            row.setDouble(betaKey, beta);
            return Optional.empty();
        });
    });
});

Array.of("Beta", "Alpha").forEach(coeff -> {
    final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
    Chart.hist(coeffResults, 250, chart -> {
        chart.axes().domain().label().withText("Coefficient Estimate");
        chart.title().withText(coeff + " Histograms of " + regressionCount + " Regressions");
        chart.subtitle().withText(coeff + " Variance decreases as sample size increases");
        chart.legend().on().bottom();
        chart.show(700, 400);
    });
});
```

<p align="center">
    <img src="../../images/ols/ols-Beta-consistency.png"/>
    <img src="../../images/ols/ols-Alpha-consistency.png"/>
</p>


It is clear from the above plots that as sample size increases, the variance in the estimates decreases, which is what we expect if the
estimator is consistent. The bar charts below summarize the change in variance for each of the coefficients, and is follow by the code
that generates these plots.

<p align="center">
    <img src="../../images/ols/ols-consistency.png"/>
</p>

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