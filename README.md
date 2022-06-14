# [Telegram X](https://play.google.com/store/apps/details?id=org.thunderdog.challegram) — a slick experimental Telegram client based on [TDLib](https://core.telegram.org/tdlib).

![Telegram X](/images/feature.png)

This is the complete source code and the build instructions for the official alternative Android client for the Telegram messenger, based on the [Telegram API](https://core.telegram.org/api) and the [MTProto](https://core.telegram.org/mtproto) secure protocol via [TDLib](https://github.com/TGX-Android/tdlib).

* [**Telegram X** on Google Play](http://play.google.com/store/apps/details?id=org.thunderdog.challegram)
* [Subscribe to Beta](https://play.google.com/apps/testing/org.thunderdog.challegram)
* [APKs and Build Info](https://t.me/tgx_log)
* [Bot to verify APK hash](https://t.me/tgx_bot)

## Build instructions

### Prerequisites

* At least **5,34GB** of free disk space: **487,10MB** for source codes and around **4,85GB** for files generated after building all variants
* **4GB** of RAM
* **macOS** or **Linux**-based operating system. **Windows** platform is not yet supported in [scripts](/scripts) that build native dependencies, however, it might be easy to patch them in order to make it work.

#### macOS

* [Homebrew](https://brew.sh)
* git with LFS, wget and sed: `$ brew install git git-lfs wget gsed`

#### Ubuntu

* git with LFS: `# apt install git git-lfs`

### Building

1. `$ git clone --recursive --depth=1 --shallow-submodules https://github.com/TGX-Android/Telegram-X tgx` — clone **Telegram X** with submodules
2. In case you forgot the `--recursive` flag, `cd` into `tgx` directory and: `$ git submodule init && git submodule update --init --recursive --depth=1`
3. Create `keystore.properties` file outside of source tree with the following properties:<br/>`keystore.file`: absolute path to the keystore file<br/>`keystore.password`: password for the keystore<br/>`key.alias`: key alias that will be used to sign the app<br/>`key.password`: key password.<br/>**Warning**: keep this file safe and make sure nobody, except you, has access to it. For production builds one could use a separate user with home folder encryption to avoid harm from physical theft
4. `$ cd tgx`
5. Run `$ scripts/./setup.sh` and follow up the instructions
6. If you specified package name that's different from the one Telegram X uses, [setup Firebase](https://firebase.google.com/docs/android/setup) and replace `google-services.json` with the one that's suitable for the `app.id` you need
7. Now you can open the project using **[Android Studio](https://developer.android.com/studio/)** or build manually from the command line: `./gradlew assembleUniversalRelease`.

#### Available flavors

* `arm64`: **arm64-v8a** build with `minSdkVersion` set to `21` (**Lollipop**)
* `arm32`: **armeabi-v7a** build
* `x64`: **x86_64** build with `minSdkVersion` set to `21` (**Lollipop**)
* `x86`: **x86** build
* `universal`: universal build that includes native bundles for all platforms.

## Reproducing public builds

In order to verify that there is no additional source code injected inside official APKs, you must use **Ubuntu 21.04** and comply with the following requirements:

1. Create user called `vk` with the home directory located at `/home/vk`
2. Clone `tgx` repository to `/home/vk/tgx`
3. Check out the specific commit you want to verify
4. In rare cases of builds that include unmerged pull requests, you must follow actions performed by [Publisher's](https://github.com/TGX-Android/Publisher/blob/main/main.js) `fetchPr` and `squashPr` tasks
5. `cd` into `tgx` folder and install dependencies: `# apt install $(cat reproducible-builds/dependencies.txt)`
6. Follow up the build instruction from the previous section
7. Run `$ apkanalyzer apk compare --different-only <remote-apk> <reproduced-apk>`
8. If only signature files and metadata differ, build reproduction is successful.

In future build reproduction might become easier. Here's a list of related PR-welcome TODOs:

* Project path must not affect the resulting `.so` files, so user & project location requirement could be removed
* When building native binaries on **macOS**, `.comment` ELF section differs from the one built with **Linux** version of NDK. It must be removed or made deterministic without any side-effects like breaking `native-debug-symbols.zip` (or should be reported to NDK team?)
* It might be a good idea to use `--build-id=0x<commit>` instead of `--build-id=none`
* Checksums of cold APK builds always differ, even though the same keystore applied and generated inner APK contents do not differ. Real cause must be investigated and fixed, if possible.<br/>To generate cold build, invoke `$ scripts/./reset.sh` and `$ scripts/./setup.sh --skip-sdk-setup`.<br/>**Warning**: this will also reset changes inside some of the submodules ([ffmpeg](/app/jni/thirdparty/ffmpeg), [libvpx](/app/jni/thirdparty/libvpx), [webp](/app/jni/thirdparty/webp), [opus](/app/jni/thirdparty/opus) and [ExoPlayer](/app/jni/thirdparty/exoplayer))
* Move local pull requests squash-merging from [Publisher](https://github.com/TGX-Android/Publisher) to some script inside this repository to make reproduction of builds that include them easier.

<i>PS: [Docker](https://www.docker.com) is not considered an option, as it just hides away these tasks, and requires that all published APKs must be built using it.</i>

## Verifying side-loaded APKs

If you downloaded **Telegram X** APK from somewhere and would like to simply verify whether it's an original APK without any injected malicious source code, you need to get checksum (`SHA-256`, `SHA-1` or `MD5`) of the downloaded APK file and find whether it corresponds to any known **Telegram X** version.

In order to obtain **SHA-256** of the APK:

* `$ sha256sum <path-to-apk>` on **Ubuntu**
* `$ shasum -a 256 <path-to-apk>` on **macOS**

Once obtained, there are three ways to find out the commit for the specific checksum:

* Sending checksum to [`@tgx_bot`](https://t.me/tgx_bot)
* Searching for a checksum in [`@tgx_log`](https://t.me/tgx_log). You can do so without need in installing any Telegram client by using this URL format: [`https://t.me/s/tgx_log?q={checksum}`](https://t.me/s/tgx_log?q=c541ebb0a3ae7bb6e6bd155530f375d567b8aef1761fdd942fb5d69af62e24ae) (click to see in action). Note: unpublished builds cannot be verified this way.

## License

`Telegram X` is licensed under the terms of the GNU General Public License v3.0.

For more information, see [LICENSE](/LICENSE) file.

License of components and third-party dependencies it relies on might differ, check `LICENSE` file in the corresponding folder.

### Third-party dependencies

List of third-party components used in **Telegram X** can be found [here](/docs/THIRDPARTY.md). Additionally you can check the specific commit of the third-party component used, for example, [here](/app/jni/thirdparty) and [here](/thirdparty).

## Contributions

**Telegram X** welcomes contributions. Check out [pull request template](/docs/PULL_REQUEST_TEMPLATE.md) and [guide for contributors](/docs/GUIDE.md) to learn more about Telegram X internals before creating the first pull request.

If you are a regular user and experience a problem with Telegram X, the best place to look for solution is [Telegram X chat](https://t.me/tgandroidtests) — a community with over 4 thousand members. Please do not use this repository to ask questions: if you have general issue with Telegram, refer to [FAQ](http://telegram.org/faq) or contact [Telegram Support](https://telegram.org/faq#telegram-support).