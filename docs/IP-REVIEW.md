# Asset and licensing review — 2026-10-08

## Scope and findings

Reviewed the current project file inventory, resource-loading paths, procedural renderer, bitmap font, synthesized audio, track generator, existing license, and documented reference use. No downloaded sprites, fonts, recordings, video clips, or reference-repository resources were found in the game's resource tree. Terrain textures, crowd, UI lettering and audio are generated in this project's scripts. Bikes now use a commissioned-in-session AI-generated PNG sheet, described below. The reference material stored outside the project is not part of the Web export mirror.

The earlier reference repositories informed ideas about height-field lanes and ground/air states. The session implementation record documents independently implemented rendering and physics rather than copied reference code. This inventory review is not an exhaustive code-similarity or chain-of-title investigation. The repository's existing proprietary LICENSE is retained; its ownership assertion has not been independently verified.

Added an offline CREDITS dialog exposing Engine.get_license_text(), Engine.get_copyright_info(), and Engine.get_license_info() from the actual running engine, including notices for bundled third-party components. Godot permits commercial games and requires its notices; see https://godotengine.org/license/ . Check the actual signed Android export's credits before release, including any additional SDK/plugin licenses introduced later.

## Copyright and release limits

US Copyright Office guidance distinguishes game ideas and methods from protected artwork, text, software and audiovisual expression: https://www.copyright.gov/register/tx-games.html . Original assets reduce copying concerns, but their originality alone does not establish that the overall presentation is legally clear. Avoid reproducing another game's distinctive screen compositions, sprites, tracks, branding or marketing.

This is a technical provenance review, not legal clearance or a guarantee of non-infringement. A qualified IP lawyer should review the final audiovisual presentation and ownership before commercial release if legal assurance is needed. No comprehensive trademark clearance has been performed for MOTO THRASH or TripperDeeLabs. Game names raise separate trademark questions; search similar names and related goods, not merely exact matches: https://www.uspto.gov/trademarks/search/comprehensive-clearance-search-similar-trademarks . Do not claim Nintendo affiliation or use its branding in store art.

## Preliminary name-search finding

A general web search on 2026-10-08 surfaced an existing racing game named **Dirt Rush**, including a ModDB download listing: https://www.moddb.com/games/dirt-rush/downloads and https://www.moddb.com/games/dirt-rush/downloads/dirt-rush-v3-mac . This is evidence of another use, not a determination of trademark ownership, registration, priority, or infringement. The game was subsequently renamed Cinderspoke. Exact-name web searches for Cinderspoke and Cinder Spoke did not surface an obvious game-title match in the returned results; this is only a preliminary screen, not trademark clearance. The replacement name also requires proper clearance before release.

The user selected **Moto Thrash: Retro Racing** as the final app title, with **MOTO THRASH** on the title screen. Preliminary exact-phrase web searching for Moto Thrash did not surface an obvious game-title match in returned results. This does not establish trademark availability; the existing clearance limitations still apply. Cinderspoke is a superseded working title.

## Rider sprite-sheet update

`assets/sprites/moto-rider-sheet-v1.png` was created in-session using the built-in OpenAI image-generation tool from the adjacent `.prompt.txt` specification. No reference images were supplied to the generation call; the prompt requested an original design and explicitly excluded existing-game sprite recreation and branding. The source PNG is preserved, with all eight frames and alpha; runtime atlas extraction and palette swaps supply game poses/colors. AI generation is not a guarantee of originality, non-infringement, exclusive rights or copyrightability; include this asset in the final legal review.

## Tree sprite-sheet update

`assets/sprites/trackside-trees-v1.png` was created with the built-in OpenAI image-generation tool from its adjacent prompt file, without reference images. It supplies eight original-request tree designs; runtime cropping, nearest-neighbor resizing and alpha handling prepare display frames. The same AI-asset review limitations stated above apply.

## Track surface sprite-sheet update

`assets/sprites/track-surfaces-v1.png` was generated with the built-in OpenAI image-generation tool from the adjacent prompt, without reference images. It supplies oil, mud and boost track decals. Retain the same provenance and clearance qualifications as the rider/tree assets.

## Stadium fan sprite-sheet update

`assets/sprites/stadium-fans-v1.png` was generated with the built-in OpenAI image-generation tool from its adjacent prompt, without reference images. The eight designs depict generic Sasquatch, alien, cat, dog, mouse and yeti fans; the prompt excluded famous characters and branding. Runtime atlas cropping and staggered bobbing create the two-row crowd. The same AI-asset review limitations above apply.

`assets/sprites/stadium-fans-v2.png` adds twelve generated designs, with its exact prompt adjacent. The requested look-and-find and horror themes were translated into a checkered tourist and original owl-mask, mushroom-monster and fox-mask fans. No reference images were supplied; the prompt explicitly excluded recognizable franchise costumes and accessories. This records design intent and provenance, not legal clearance.

`assets/sprites/stadium-people-v1.png` adds eight generated generic human spectators. It was created with built-in image generation without reference images; its exact prompt is stored alongside the PNG. The same provenance and review qualifications apply.

`assets/sprites/stadium-people-v2.png` is a built-in image-generation edit using the project's v1 human sheet as its sole reference. It converts those designs into simplified silhouettes; the exact edit prompt is adjacent. Runtime preparation makes them solid-color, coarsely pixelated crowd extras.

## Jump hazards

`assets/sprites/jump-hazards-v1.png` was generated with built-in image generation without reference images, using its adjacent prompt. It depicts generic pools, a river, crocodiles, lava and unbranded vehicles. The same asset-review qualifications apply. Stick-figure spectators are original code-drawn smiling round-headed figures with thin limbs, informed by the user's pose/style reference rather than imported from that image.
