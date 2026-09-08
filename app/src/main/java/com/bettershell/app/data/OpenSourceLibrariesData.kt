package com.bettershell.app.data

/**
 * 开源项目详细许可项元数据
 */
data class OpenSourceLibraryDetail(
    val id: String,
    val name: String,
    val artifact: String,
    val author: String,
    val version: String,
    val licenseName: String,
    val licenseUrl: String,
    val licenseContent: String
)

object OpenSourceLibrariesData {

    private const val APACHE_2_0_TEXT = """Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/

TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

1. Definitions.

"License" shall mean the terms and conditions for use, reproduction, and distribution as defined by Sections 1 through 9 of this document.

"Licensor" shall mean the copyright owner or entity authorized by the copyright owner that is granting the License.

"Legal Entity" shall mean the union of the acting entity and all other entities that control, are controlled by, or are under common control with that entity. For the purposes of this definition, "control" means (i) the power, direct or indirect, to cause the direction or management of such entity, whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial ownership of such entity.

"You" (or "Your") shall mean an individual or Legal Entity exercising permissions granted by this License.

"Source" form shall mean the preferred form for making modifications, including but not limited to software source code, documentation source, and configuration files.

"Object" form shall mean any form resulting from mechanical transformation or translation of a Source form, including but not limited to compiled object code, generated documentation, and conversions to other media types.

"Work" shall mean the work of authorship, whether in Source or Object form, made available under the License, as indicated by a copyright notice that is included in or attached to the work.

"Derivative Works" shall mean any work, whether in Source or Object form, that is based on (or derived from) the Work and for which the editorial revisions, annotations, elaborations, or other modifications represent, as a whole, an original work of authorship.

2. Grant of Copyright License. Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform, sublicense, and distribute the Work and such Derivative Works in Source or Object form.

3. Grant of Patent License. Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer the Work.

4. Redistribution. You may reproduce and distribute copies of the Work or Derivative Works thereof in any medium, with or without modifications, and in Source or Object form, provided that You meet the following conditions:
(a) You must give any other recipients of the Work or Derivative Works a copy of this License; and
(b) You must cause any modified files to carry prominent notices stating that You changed the files; and
(c) You must retain, in the Source form of any Derivative Works that You distribute, all copyright, patent, trademark, and attribution notices from the Source form of the Work; and
(d) If the Work includes a "NOTICE" text file as part of its distribution, then any Derivative Works that You distribute must include a readable copy of the attribution notices contained within such NOTICE file.

5. Submission of Contributions. Unless You explicitly state otherwise, any Contribution intentionally submitted for inclusion in the Work by You to the Licensor shall be under the terms and conditions of this License, without any additional terms or conditions.

6. Trademarks. This License does not grant permission to use the trade names, trademarks, service marks, or product names of the Licensor, except as required for reasonable and customary use in describing the origin of the Work.

7. Disclaimer of Warranty. Unless required by applicable law or agreed to in writing, Licensor provides the Work on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

8. Limitation of Liability. In no event and under no legal theory shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use the Work.

9. Accepting Warranty or Additional Liability. While redistributing the Work or Derivative Works thereof, You may choose to offer, and charge a fee for, acceptance of support, warranty, indemnity, or other liability obligations and/or rights consistent with this License."""

    private const val MIT_TEXT = """MIT License

Copyright (c) 2020-2026 Tabler Authors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE."""

    private const val ISC_TEXT = """ISC License

Copyright (c) 2022-2026, Lucide Contributors

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE."""

    private const val BSD_TEXT = """Revised BSD License

Copyright (c) 2002-2026, mwiede & JSch Project Authors. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."""

    val LIBRARIES: List<OpenSourceLibraryDetail> = listOf(
        OpenSourceLibraryDetail(
            id = "kestrel",
            name = "Kestrel",
            artifact = "owocc.kestrel",
            author = "owocc",
            version = "1.0",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "tabler-icons",
            name = "Tabler Icons Compose",
            artifact = "br.com.devsrsouza.compose.icons:tabler-icons",
            author = "DevSrSouza / Tabler Authors",
            version = "1.1.1",
            licenseName = "MIT License",
            licenseUrl = "https://opensource.org/licenses/MIT",
            licenseContent = MIT_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "lucide-icons",
            name = "Lucide Icons",
            artifact = "lucide-icons",
            author = "Lucide Contributors",
            version = "0.470.0",
            licenseName = "ISC License",
            licenseUrl = "https://opensource.org/licenses/ISC",
            licenseContent = ISC_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "compose-material3",
            name = "Compose Material 3 Library",
            artifact = "androidx.compose.material3:material3",
            author = "Google",
            version = "1.3.1",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "compose-ui",
            name = "Compose UI Library",
            artifact = "androidx.compose.ui:ui",
            author = "Google",
            version = "1.7.5",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "compose-icons-extended",
            name = "Material Icons Extended",
            artifact = "androidx.compose.material:material-icons-extended",
            author = "Google",
            version = "1.7.5",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "kotlinx-coroutines",
            name = "Kotlin Coroutines Android",
            artifact = "org.jetbrains.kotlinx:kotlinx-coroutines-android",
            author = "JetBrains",
            version = "1.9.0",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "kotlinx-serialization",
            name = "Kotlinx Serialization JSON",
            artifact = "org.jetbrains.kotlinx:kotlinx-serialization-json",
            author = "JetBrains",
            version = "1.7.3",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "navigation-compose",
            name = "Navigation Compose Library",
            artifact = "androidx.navigation:navigation-compose",
            author = "Google",
            version = "2.8.5",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "jsch",
            name = "JSch SSH Client (mwiede fork)",
            artifact = "com.github.mwiede:jsch",
            author = "mwiede & JCraft",
            version = "0.2.21",
            licenseName = "Revised BSD License",
            licenseUrl = "https://opensource.org/licenses/BSD-3-Clause",
            licenseContent = BSD_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "core-ktx",
            name = "AndroidX Core KTX",
            artifact = "androidx.core:core-ktx",
            author = "Google",
            version = "1.15.0",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "activity-compose",
            name = "Activity Compose Library",
            artifact = "androidx.activity:activity-compose",
            author = "Google",
            version = "1.9.3",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        ),
        OpenSourceLibraryDetail(
            id = "lifecycle-compose",
            name = "Lifecycle Runtime Compose",
            artifact = "androidx.lifecycle:lifecycle-runtime-compose",
            author = "Google",
            version = "2.8.7",
            licenseName = "Apache License 2.0",
            licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0",
            licenseContent = APACHE_2_0_TEXT
        )
    )

    fun findById(id: String): OpenSourceLibraryDetail? {
        return LIBRARIES.firstOrNull { it.id == id }
    }
}
