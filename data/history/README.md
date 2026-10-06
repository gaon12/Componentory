# Reviewed public Android UI source records

These CSV records are source inputs for the offline app catalog, not generated
application assets or UI captures. They were extracted from the immutable SDK
commit in `provenance.json` with `scripts.history inventory` and reviewed against
the public signatures. Button begins at API 1, Switch at API 14, Toolbar at API 21,
and Preference becomes deprecated at API 29. Abstract classes remain explicit.
The 36 snapshots contain 3,269 version/class records under the documented classifier.

The provenance file retains original source URLs and hashes, the classifier scope,
and the complete table hash. API 35 and 36 have identical signature bytes in this
source snapshot. The app reports that fact instead of inventing additions. API 37
has no record in this audited snapshot and is not offered as a historical version.

To update, export to a fresh ignored directory, review classifier changes and
version differences against the original sources, and replace these two reviewed
inputs together. Gradle packages them into generated assets under `app/build`;
an ordinary build does not download sources or run Python. Original resource
graphs, archives, caches, and screenshots belong outside Git.

API labels follow the [Android API level table](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels).
An API revision label does not identify an OS patch release or prove original UI
appearance. See [the source analysis workflow](../../docs/android-history.md).
