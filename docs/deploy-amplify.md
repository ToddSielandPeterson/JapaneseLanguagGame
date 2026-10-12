# Put the game on the web with AWS Amplify

This page is for someone who wants the sentence game online and does not write Kotlin. Amplify builds the site from this repository and gives you a web address. You do not install Java on your own computer.

The site is one page. It works in a current Chrome, Edge, Firefox, or Safari. Old browsers are not supported.

There is no `stage` branch in this repository. Use `main` as the live site. A second, staging copy is optional and is described below.

## Before you start

- An AWS account you can sign in to.
- This project on GitHub (the same repository that contains this file).
- Permission in Amplify to connect that GitHub repository.

## Connect the repository

1. Sign in to the [AWS Amplify console](https://console.aws.amazon.com/amplify/).
2. Choose **Create new app** (sometimes labeled **Deploy an app** or **Host web app**).
3. Choose **GitHub** and authorize AWS to see your repositories if it asks.
4. Pick this repository.
5. For the branch, pick **`main`**. That branch is the production site: every push to `main` updates the live game.
6. If Amplify asks for an app root or a monorepo path, leave it as the repository root. Do not pick a subfolder.
7. If it asks you to choose a framework, choose the option that means “no framework” or that it will use the build file already in the repository. Do not pick React, Next.js, or another starter. This project is not one of those.

## Use the build file that is already here

The repository contains `amplify.yml`. That file tells Amplify what to do:

- Install Amazon Corretto 21 (a Java development kit) if the build machine does not already have Java 17 or 21.
- Run `./gradlew wasmJsBrowserDistribution`.
- Publish the folder `build/dist/wasmJs/productionExecutable`.

When the wizard shows build settings, they should match that file. If Amplify offers to replace `amplify.yml`, keep the copy from the repository.

You do not type the Gradle command yourself. The first build is slow: Amplify downloads Gradle, Kotlin, and Compose. Later builds reuse a cache.

Choose **Save and deploy** (or **Next**, then **Save and deploy**). Wait until the build is green. Amplify then shows a URL that ends in `amplifyapp.com`. Open it. You should see Sentence Builder, with small readings above the kanji.

## Optional: a staging copy

This repository does not have a branch named `stage`, so there is nothing to connect until you create one.

When you want a second site for trying changes before they go live:

1. In GitHub, create a branch named `stage` from `main`.
2. In Amplify, open the app, choose **Hosting**, then **Branch settings** (or **Connect branch**).
3. Connect the `stage` branch. Amplify uses the same `amplify.yml`.
4. Treat the `stage` URL as the try-out site, and `main` as the public site.

## Optional: a preview for each pull request

Previews give every pull request its own temporary URL.

1. Open the app in Amplify.
2. Choose **Hosting**, then **Previews**.
3. Enable previews and allow the GitHub connection if asked.
4. Open a pull request. Amplify adds a link to a temporary copy of that branch.

The preview uses the same build as production. Closing the pull request removes the preview.

## Optional: a password

This password is a gate in front of the whole website. It is not a player account inside the game. Score is still stored only in that browser.

1. Open the app in Amplify.
2. Choose **Hosting**, then **Access control**.
3. Choose the branch (`main`, or `stage` if you added it).
4. Turn on restricted access and set a username and password.
5. Save. Amplify applies the password on the next request. Share the username and password with the people who should open the site.

To take the password off, turn access control off for that branch.

## If the page is blank

The game is a WebAssembly page. The browser will only start the `.wasm` file when the site tells it the type is `application/wasm`. This repository includes `customHttp.yml` so Amplify sends that type for every file ending in `.wasm`. After the first successful deploy, check **Hosting → Custom headers**. You should see a rule for `**/*.wasm`. Leave it in place. The file in the repository overrides whatever is typed only in the console, so edit `customHttp.yml` and redeploy if you need to change it.

Check the type yourself:

1. Open the site.
2. Press F12 and choose **Network**.
3. Refresh the page.
4. Click the file whose name ends in `.wasm`.
5. The type should be `wasm` or `application/wasm`.

If that file is `text/html` instead, a rewrite is sending it to `index.html`. Open **Hosting → Rewrites and redirects**. This game does not need a single-page rewrite: the site is already one page at the root. Either delete a catch-all rewrite, or change it so files ending in `wasm` (and `js`, `css`, `json`, `ttf`) are not rewritten. A pattern that leaves those files alone looks like this:

```
</^[^.]+$|\.(?!(css|gif|ico|jpg|js|png|txt|svg|woff|woff2|ttf|map|json|wasm)$)([^.]+$)/>
```

Target `/index.html`, type **200 (Rewrite)**. Put that rule after any rule that serves real files. Then redeploy and check the `.wasm` type again.

## If the build fails

Open the failed build in Amplify and read the log.

- **Java / `javac` not found.** The `preBuild` section of `amplify.yml` installs Corretto 21 with `dnf` or `yum`. The build image must allow that install. A normal Amplify Hosting image does.
- **The build stops suddenly** or the log mentions running out of memory. In the Amplify app settings, raise the build instance size and start the build again. Compiling the web game needs a few gigabytes.
- **The published folder is empty or missing.** The artifacts directory in `amplify.yml` must stay `build/dist/wasmJs/productionExecutable`. Do not point it at `build` by itself.

The live site is served from the root of the Amplify URL, not from a subfolder.
