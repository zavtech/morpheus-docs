## Capital Asset Pricing Model

### Introduction

### Beta

<?prettify?>
```java
YahooReturnSource source = DataFrameSource.lookup(YahooReturnSource.class);
DataFrame<LocalDate,String> daily = source.read(options -> {
    options.withStartDate(LocalDate.of(2016, 1, 1));
    options.withEndDate(LocalDate.now());
    options.withTickers("AAPL", "SPY");
    options.daily();
});

Chart.create().withScatterPlot(daily.applyDoubles(v -> v.getDouble() * 100d), false, "SPY", chart -> {
    chart.title().withText("Regression of Daily AAPL Returns on SPY");
    chart.plot().axes().domain().label().withText("SPY Returns");
    chart.plot().axes().range(0).label().withText("AAPL Returns");
    chart.plot().axes().domain().format().withPattern("0.00'%';-0.00'%'");
    chart.plot().axes().range(0).format().withPattern("0.00'%';-0.00'%'");
    chart.plot().render(1).withDots();
    chart.plot().render(2).withDots();
    chart.plot().trend("AAPL");
    chart.legend().on().right();
    chart.show();
});
```
<p align="center">
    <img class="chart" src="../../images/yahoo/asset_beta_1.png"/>
</p>
