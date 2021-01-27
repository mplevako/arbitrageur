An implementation of a max-plus tropical algebra based algorithm for finding the best exchange rate arbitrage opportunities
among n "currencies", that requires O(n⁴) time to run under an arbitrary-precision computational model.

### Running
Ensure the latest version of [Ammonite](https://ammonite.io/) is installed, then run
```bash
$ amm arbitrageur.sc
```
in your console to see the usage message, or run the `arbitrageur` script passing it the URL of an exchange rate provider that returns rates as a JSON object with "currency code" pairs joined through the `_` character as the names and the rates as the values, e.g.:
```json
{
  "QUATLOOS_GOLDPRESSEDLATINUM": 0.0072973525628,
  "GOLDPRESSEDLATINUM_QUATLOOS": 1.570359992057584240145136,
  "NUYEN_EURODOLLAR": 2.718281828459045235360287471352662497757247093,
  "FLANIANPOBBLEBEAD_FLANIANPOBBLEBEAD": 1.0
}
```
Type, for example,
```bash
$ amm arbitrageur.sc https://fx.priceonomics.com/v1/rates/
```
to hit the ground running.