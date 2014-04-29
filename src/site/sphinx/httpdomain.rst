``sphinxcontrib-httpdomain``
============================

.. versionadded:: 1.0.4
    Extension ``sphinxcontrib-httpdomain`` is bundled within ``sphinx-maven``.

Introduction
------------

To use this extension, just add the extension to your configuration file::

    extensions = [...,'sphinxcontrib.httpdomain',...]

See https://pythonhosted.org/sphinxcontrib-httpdomain/ for the documentation.

Sample
------

.. http:get:: /users/(int:user_id)/posts/(tag)

   The posts tagged with `tag` that the user (`user_id`) wrote.

   **Example request**:

   .. sourcecode:: http

      GET /users/123/posts/web HTTP/1.1
      Host: example.com
      Accept: application/json, text/javascript

   :query offset: offset number. default is 0
   :query limit: limit number. default is 30
   :reqheader Accept: the response content type depends on
                      ``Accept`` header
   :reqheader Authorization: optional OAuth token to authenticate
   :resheader Content-Type: this depends on ``Accept``
                            header of request
   :statuscode 200: no error
   :statuscode 404: there's no user
   
   

